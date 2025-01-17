/*
 * Copyright (c) 2019, Xiaomi, Inc.  All rights reserved.
 * This source code is licensed under the Apache License Version 2.0, which
 * can be found in the LICENSE file in the root directory of this source tree.
 */

package com.xiaomi.thain.common.utils;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.ImmutableMap;
import com.xiaomi.thain.common.entity.X5;
import com.xiaomi.thain.common.exception.ThainRuntimeException;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * @author miaoyu
 * @date 19-6-26 下午12:46
 */
public class X5Utils {

    private X5Utils() {

    }

    private static final String X5_JSON_SCHEMA = "{\n" +
            "             \"$schema\": \"http://json-schema.org/draft-07/schema#\",\n" +
            "             \"$ref\": \"#/definitions/X5\",\n" +
            "             \"definitions\": {\n" +
            "               \"X5\": {\n" +
            "                 \"type\": \"object\",\n" +
            "                 \"additionalProperties\": false,\n" +
            "                 \"properties\": {\n" +
            "                   \"header\": {\n" +
            "                     \"$ref\": \"#/definitions/Header\"\n" +
            "                                         },\n" +
            "                   \"body\": {\n" +
            "                     \"type\": \"string\"\n" +
            "                                         }\n" +
            "                 },\n" +
            "                 \"required\": [\n" +
            "                   \"body\",\n" +
            "                   \"header\"\n" +
            "                               ],\n" +
            "                 \"title\": \"X5\"\n" +
            "                             },\n" +
            "               \"Header\": {\n" +
            "                 \"type\": \"object\",\n" +
            "                 \"additionalProperties\": false,\n" +
            "                 \"properties\": {\n" +
            "                   \"sign\": {\n" +
            "                     \"type\": \"string\"\n" +
            "                                         },\n" +
            "                   \"appid\": {\n" +
            "                     \"type\": \"string\"\n" +
            "                                         },\n" +
            "                   \"url\": {\n" +
            "                     \"type\": \"string\"\n" +
            "                                         },\n" +
            "                   \"method\": {\n" +
            "                     \"type\": \"string\"\n" +
            "                                         }\n" +
            "                 },\n" +
            "                 \"required\": [\n" +
            "                   \"appid\",\n" +
            "                   \"sign\"\n" +
            "                               ],\n" +
            "                 \"title\": \"Header\"\n" +
            "                             }\n" +
            "             }\n" +
            "           }";

    private static final JSONObject RAW_SCHEMA = new JSONObject(new JSONTokener(X5_JSON_SCHEMA));
    private static final Schema SCHEMA = SchemaLoader.load(RAW_SCHEMA);
    private static final Base64.Decoder DECODER = Base64.getDecoder();
    private static final Base64.Encoder ENCODER = Base64.getEncoder();

    public static Map<String, String> buildX5Request(String appid, String key, Map<String, Object> body) {
        return buildX5Request(appid, key, JSON.toJSONString(body));
    }

    public static Map<String, String> buildX5Request(String appid, String key, String jsonBody) {
        String sign = DigestUtils.md5Hex((appid + jsonBody + key).getBytes(StandardCharsets.UTF_8)).toUpperCase();
        Map<String, String> header = new HashMap<>(4);
        header.put("appid", appid);
        header.put("sign", sign);
        Map data = ImmutableMap.of("header", header, "body", jsonBody);
        String dataStr = new JSONObject(data).toString();
        String base64 = new String(ENCODER.encode(dataStr.getBytes(StandardCharsets.UTF_8)));
        base64 = base64.replaceAll("\n", "");
        return ImmutableMap.of("data", base64);

    }

    public static X5 getX5(String data) {
        if (StringUtils.isEmpty(data)) {
            throw new ThainRuntimeException("empty data");
        }
        String dataString = new String(DECODER.decode(data), StandardCharsets.UTF_8);
        SCHEMA.validate(new JSONObject(dataString));
        return JSON.parseObject(dataString, X5.class);
    }

    public static boolean validate(X5 x5, String appkey) {
        String md5String = DigestUtils.md5Hex((x5.getHeader().getAppid() + x5.getBody() + appkey).getBytes(StandardCharsets.UTF_8))
                .toUpperCase();
        return md5String.equals(x5.getHeader().getSign());
    }
}
