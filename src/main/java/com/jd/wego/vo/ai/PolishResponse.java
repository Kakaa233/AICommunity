package com.jd.wego.vo.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

/**
 * 文章润色响应
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PolishResponse {
    private String polishedText;
    private String changes;
    private String polishedTitle;
    private String summary;
    private List<String> tags;
}
