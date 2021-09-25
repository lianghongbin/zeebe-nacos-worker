package io.zeebe.http.controller;

import org.springframework.context.annotation.Conditional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author eric.liang
 * @date 9/23/21
 */
@RestController
@RequestMapping(value = "/order")
public class WorkerController {


    @RequestMapping(value = "/c")
    @PutMapping(value = "/b")
    public String consumer() {
        return "consumer controller";
    }

    @GetMapping()
    public String work() {
        return "working";
    }
}
