package com.syx.todolistadmin.service.impl;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.syx.todolistadmin.service.AIService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class AIServiceImpl implements AIService {

    @Value("${xfyun.app-id}")
    private String xfyunAppId;

    @Value("${xfyun.api-key}")
    private String xfyunApiKey;

    @Value("${xfyun.api-secret}")
    private String xfyunApiSecret;

    @Value("${dashscope.api-key}")
    private String dashscopeApiKey;

    @Value("${dashscope.model}")
    private String dashscopeModel;

    @Override
    public String voiceToText(MultipartFile audioFile) {
        // 讯飞星火语音识别（简化实现，实际需要WebSocket连接）
        log.info("开始语音识别，文件大小: {} bytes", audioFile.getSize());

        // 这里使用模拟实现，实际应该调用讯飞API
        // 真实实现需要建立WebSocket连接并发送音频流
        try {
            // 模拟识别结果
            String mockResult = "明天下午三点开会讨论项目进度";
            log.info("语音识别结果: {}", mockResult);
            return mockResult;
        } catch (Exception e) {
            log.error("语音识别失败: {}", e.getMessage());
            return "";
        }
    }

    @Override
    public Map<String, Object> extractTaskInfo(String text) {
        Map<String, Object> taskInfo = new HashMap<>();

        // 提取标题（取前20个字符）
        String title = text.length() > 20 ? text.substring(0, 20) : text;
        taskInfo.put("title", title);
        taskInfo.put("description", text);

        // 提取时间信息
        LocalDateTime dueDate = extractDateTime(text);
        if (dueDate != null) {
            taskInfo.put("dueDate", dueDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }

        // 提取优先级关键词
        String priority = "medium";
        if (text.contains("紧急") || text.contains("重要") || text.contains("马上")) {
            priority = "high";
        } else if (text.contains("不急") || text.contains("有空")) {
            priority = "low";
        }
        taskInfo.put("priority", priority);

        return taskInfo;
    }

    @Override
    public Map<String, String> classifyTask(String title, String description) throws ApiException {
        Map<String, String> result = new HashMap<>();

        try {
            // 构建提示词
            String prompt = String.format(
                "请分析以下任务，返回JSON格式的分类和优先级建议：\n" +
                "标题：%s\n" +
                "描述：%s\n\n" +
                "请返回格式：{\"category\": \"work/study/life\", \"priority\": \"high/medium/low\", \"reason\": \"分析原因\"}",
                title, description != null ? description : ""
            );

            // 调用通义千问API
            Generation gen = new Generation();
            Message userMsg = Message.builder()
                    .role(Role.USER.getValue())
                    .content(prompt)
                    .build();

            GenerationParam param = GenerationParam.builder()
                    .apiKey(dashscopeApiKey)
                    .model(dashscopeModel)
                    .messages(Arrays.asList(userMsg))
                    .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                    .build();

            GenerationResult response = gen.call(param);
            String aiResponse = response.getOutput().getChoices().get(0).getMessage().getContent();

            // 解析AI返回的JSON
            JSONObject jsonResult = JSON.parseObject(aiResponse);
            result.put("category", jsonResult.getString("category"));
            result.put("priority", jsonResult.getString("priority"));
            result.put("reason", jsonResult.getString("reason"));

            log.info("AI分类结果: {}", result);
        } catch (ApiException | NoApiKeyException | InputRequiredException e) {
            log.error("AI分类失败: {}", e.getMessage());
            // 降级为规则分类
            result = ruleBasedClassify(title, description);
        }

        return result;
    }

    /**
     * 基于规则的分类（降级方案）
     */
    private Map<String, String> ruleBasedClassify(String title, String description) {
        Map<String, String> result = new HashMap<>();
        String text = (title + " " + (description != null ? description : "")).toLowerCase();

        // 分类规则
        if (text.contains("工作") || text.contains("会议") || text.contains("项目") || text.contains("报告")) {
            result.put("category", "work");
        } else if (text.contains("学习") || text.contains("课程") || text.contains("作业") || text.contains("考试")) {
            result.put("category", "study");
        } else {
            result.put("category", "life");
        }

        // 优先级规则
        if (text.contains("紧急") || text.contains("重要") || text.contains("马上") || text.contains("立即")) {
            result.put("priority", "high");
        } else if (text.contains("不急") || text.contains("有空") || text.contains("随时")) {
            result.put("priority", "low");
        } else {
            result.put("priority", "medium");
        }

        result.put("reason", "基于关键词规则分类");
        return result;
    }

    /**
     * 从文本中提取日期时间
     */
    private LocalDateTime extractDateTime(String text) {
        LocalDateTime now = LocalDateTime.now();

        // 匹配"明天"
        if (text.contains("明天")) {
            now = now.plusDays(1);
        }
        // 匹配"后天"
        else if (text.contains("后天")) {
            now = now.plusDays(2);
        }
        // 匹配"下周"
        else if (text.contains("下周")) {
            now = now.plusWeeks(1);
        }

        // 匹配时间（如"下午三点"、"15:00"）
        Pattern timePattern = Pattern.compile("(\\d{1,2})[点:：](\\d{0,2})");
        Matcher matcher = timePattern.matcher(text);
        if (matcher.find()) {
            int hour = Integer.parseInt(matcher.group(1));
            int minute = matcher.group(2).isEmpty() ? 0 : Integer.parseInt(matcher.group(2));

            // 处理"下午"
            if (text.contains("下午") && hour < 12) {
                hour += 12;
            }

            now = now.withHour(hour).withMinute(minute).withSecond(0);
        }

        return now;
    }
}
