package com.example.demo.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.demo.service.Sign;
import jdk.nashorn.internal.runtime.options.Option;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@RestController
public class TestController {

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    RedisTemplate redisTemplate;

    @GetMapping("/test")
    public String sign(@RequestParam("url") String url) {

        String ticket =
        redisTemplate.opsForValue().get("jsapi") == null ? "" : redisTemplate.opsForValue().get("jsapi").toString();
        if (StringUtils.isEmpty(ticket)) {
            Map<String, String> reqMap = new HashMap<>(2);
            reqMap.put("appid", "wx15e89805429d4fcd");
            reqMap.put("secret", "07a1d821a108789eb424bbb0ec392b1f");
            System.out.println(JSON.toJSONString(reqMap));
            JSONObject tokenObj = restTemplate.getForObject("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid={appid}&secret={secret}", JSONObject.class, reqMap);
            String access_token = tokenObj.getString("access_token");
            JSONObject jspapi_ticket = restTemplate.getForObject("https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token={0}&type=jsapi", JSONObject.class, access_token);
            ticket = jspapi_ticket.getString("ticket");
            System.out.println(ticket);
            redisTemplate.opsForValue().set("jsapi", ticket, 115, TimeUnit.MINUTES);
        }
        Map<String, String> sign = Sign.sign(ticket, url);
        return JSON.toJSONString(sign);
    }
}