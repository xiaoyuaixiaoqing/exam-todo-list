package com.syx.todolistadmin.controller;

import com.syx.todolistadmin.common.Result;
import com.syx.todolistadmin.service.AIService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {

    private final AIService aiService;

    /**
     * 语音转任务
     */
    @PostMapping("/voice-to-task")
    public Result<Map<String, Object>> voiceToTask(@RequestParam("audio") MultipartFile audioFile) {
        // 语音识别
        String text = aiService.voiceToText(audioFile);
        if (text == null || text.isEmpty()) {
            return Result.error("语音识别失败");
        }

        // 提取任务信息
        Map<String, Object> taskInfo = aiService.extractTaskInfo(text);
        return Result.success(taskInfo);
    }

    /**
     * 智能分类
     */
    @PostMapping("/classify")
    public Result<Map<String, String>> classifyTask(@RequestBody Map<String, String> request) {
        String title = request.get("title");
        String description = request.get("description");

        if (title == null || title.isEmpty()) {
            return Result.error("标题不能为空");
        }

        Map<String, String> classification = aiService.classifyTask(title, description);
        return Result.success(classification);
    }
}
