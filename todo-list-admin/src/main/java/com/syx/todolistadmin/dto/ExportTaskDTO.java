package com.syx.todolistadmin.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportTaskDTO {
    @ExcelProperty("任务标题")
    private String title;

    @ExcelProperty("任务描述")
    private String description;

    @ExcelProperty("状态")
    private String status;

    @ExcelProperty("优先级")
    private String priority;

    @ExcelProperty("截止日期")
    private String dueDate;

    @ExcelProperty("分类")
    private String category;

    @ExcelProperty("标签")
    private String tags;

    public static ExportTaskDTO fromTask(com.syx.todolistadmin.entity.Task task) {
        String statusName = getStatusName(task.getStatus());
        String priorityName = getPriorityName(task.getPriority());
        String dueDateStr = formatDueDate(task.getDueDate());

        return ExportTaskDTO.builder()
                .title(task.getTitle())
                .description(task.getDescription())
                .status(statusName)
                .priority(priorityName)
                .dueDate(dueDateStr)
                .category(task.getCategory())
                .tags(task.getTags())
                .build();
    }

    private static String getStatusName(Integer status) {
        if (status == null) return "";
        return switch (status) {
            case 0 -> "待办";
            case 1 -> "未完成";
            case 2 -> "已完成";
            case 4 -> "已超期";
            default -> "";
        };
    }

    private static String getPriorityName(Integer priority) {
        if (priority == null) return "";
        return switch (priority) {
            case 1 -> "低";
            case 2 -> "中";
            case 3 -> "高";
            default -> "";
        };
    }

    private static String formatDueDate(LocalDateTime dueDate) {
        if (dueDate == null) return "";
        return dueDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
