package io.zeebe.http.metadata;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
public class AnnotationScanner{

    private final ApplicationContext context;
    private final NacosDiscoveryProperties properties;

    public AnnotationScanner(ApplicationContext context, NacosDiscoveryProperties properties) {
        this.context = context;
        this.properties = properties;
    }

    public void scan() {
        StringBuilder urlPrefix = new StringBuilder("");
        StringBuilder orgMethod = new StringBuilder();

        Map<String, Object> objectMap = context.getBeansWithAnnotation(RestController.class);
        objectMap.forEach((k, v) -> {
            RequestMapping requestMapping = AnnotationUtils.findAnnotation(v.getClass(), RequestMapping.class);
            if (requestMapping != null) {
                urlPrefix.append(getValue(requestMapping));
                orgMethod.append(getMethod(requestMapping));
            }

            Method[] methods = ReflectionUtils.getAllDeclaredMethods(v.getClass());
            for (Method method : methods) {
                StringBuilder url = new StringBuilder(urlPrefix);
                RequestMapping r = method.getAnnotation(RequestMapping.class); //如果方法上直接有 RequestMapping注解
                if (r != null) {
                    url.append(getValue(r));
                    url.append(";");

                    String tempM = getMethod(r);
                    if ("".equals(tempM)) {
                        url.append(orgMethod);
                    } else {
                        url.append(tempM);
                    }

                    properties.getMetadata().put(method.getName(), url.toString());
                    continue;
                }

                for (Annotation a : method.getDeclaredAnnotations()) {
                    r = a.annotationType().getDeclaredAnnotation(RequestMapping.class);
                    if (r != null) {
                        String[] values = (String[]) getAnnotationValue(a, "value");
                        String tempV = values.length == 0 ? "" : values[0];

                        url.append(tempV);
                        url.append(";");

                        String tempM = getMethod(r);
                        if ("".equals(tempM)) {
                            url.append(orgMethod);
                        } else {
                            url.append(tempM);
                        }

                        properties.getMetadata().put(method.getName(), url.toString());
                        break;
                    }
                }
            }
        });

    }

    public static Object getAnnotationValue(Annotation annotation, String property) {
        Object result = null;
        if (annotation != null) {
            InvocationHandler handler = Proxy.getInvocationHandler(annotation);
            Map map = (Map) getFieldValue(handler, "memberValues");
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
