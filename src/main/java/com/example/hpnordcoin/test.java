package com.example.hpnordcoin;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class test {
    @RequestMapping("/")
    public String index() {
    return "This is a test";
}

}
