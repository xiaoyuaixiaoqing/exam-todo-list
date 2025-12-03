package com.syx.todolistadmin.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface AIService {

    /**
     * 语音识别：将音频文件转为文字
     */
    String voiceToText(MultipartFile audioFile);

    /**
     * 从文本中提取任务信息
     */
    Map<String, Object> extractTaskInfo(String text);

    /**
     * 智能分类：根据任务标题和描述预测分类和优先级
     */
    Map<String, String> classifyTask(String title, String description);
}
