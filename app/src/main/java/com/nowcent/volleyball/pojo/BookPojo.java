package com.nowcent.volleyball.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author orangeboy
 * @version 1.0
 * @date 2020/8/2 16:51
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookPojo {
    private String token;
    private String nickname;
    private String from;
    private String to;
    private Date time;
}
