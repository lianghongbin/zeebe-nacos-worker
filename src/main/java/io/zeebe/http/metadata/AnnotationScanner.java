package io.zeebe.http.metadata;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

/**
 * @author eric.liang
 * @date 9/23/21
 */
@Component
public class AnnotationScanner implements ApplicationListener<ContextRefreshedEvent> {

    @Resource
    MetadataManager metadataManager;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {

        if (event.getApplicationContext().getParent() == null) {

            StringBuilder urlPrefix = new StringBuilder("");
            StringBuilder orgMethod = new StringBuilder();

            Map<String, Object> objectMap = event.getApplicationContext().getBeansWithAnnotation(RestController.class);
            objectMap.forEach((k, v) ->{
                RequestMapping requestMapping = AnnotationUtils.findAnnotation(v.getClass(), RequestMapping.class);
                if (requestMapping != null) {
                    urlPrefix.append(getValue(requestMapping));
                    orgMethod.append(getMethod(requestMapping));
                    System.out.println("========" + requestMapping.value()[0]);
                }

                Method[] methods = ReflectionUtils.getAllDeclaredMethods(v.getClass());
                for (Method method : methods) {
                    StringBuilder url = new StringBuilder(urlPrefix);
                    RequestMapping r = method.getAnnotation(RequestMapping.class); //如果方法上直接有 RequestMapping注解
                    if (r != null) {
                        url.append(getValue(r));
                        url.append(";");

                        String tempM = getMethod(r);
                        if (tempM.equals("")) {
                            url.append(orgMethod);
                        }
                        else {
                            url.append(tempM);
                        }

                        metadataManager.put(method.getName(), url.toString());
                        continue;
                    }

                    for(Annotation a : method.getDeclaredAnnotations()) {
                        System.out.println(a.annotationType());

                        r = a.annotationType().getDeclaredAnnotation(RequestMapping.class);
                        if (r != null) {
                            String[] values = (String[])getAnnotationValue(a, "value");
                            String tempV = values.length == 0 ? "" : values[0];

                            url.append(tempV);
                            url.append(";");

                            String tempM = getMethod(r);
                            if (tempM.equals("")) {
                                url.append(orgMethod);
                            }
                            else {
                                url.append(tempM);
                            }

                            metadataManager.put(method.getName(), url.toString());
                            System.out.println("############ " + r.annotationType());
                            break;
                        }
                    }
                }
            });
        }
    }
    public static Object getAnnotationValue(Annotation annotation, String property) {
        Object result = null;
        if (annotation != null) {
            InvocationHandler invo = Proxy.getInvocationHandler(annotation); //获取被代理的对象
            Map map = (Map) getFieldValue(invo, "memberValues");
            if (map != null) {
                result = map.get(property);
            }
        }
        return result;
    }

    public static <T> Object getFieldValue(T object, String property) {
        if (object != null && property != null) {
            Class<T> currClass = (Class<T>) object.getClass();

            try {
                Field field = currClass.getDeclaredField(property);
                field.setAccessible(true);
                return field.get(object);
            } catch (NoSuchFieldException e) {
                throw new IllegalArgumentException(currClass + " has no property: " + property);
            } catch (IllegalArgumentException e) {
                throw e;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private String getValue(RequestMapping r) {
        if (r.value().length == 0) {
            return "";
        }

        return r.value()[0];
    }

    private String getMethod(RequestMapping r) {
        if (r.method().length == 0) {
            return "";
        }

        return r.method()[0].name();
    }
}
