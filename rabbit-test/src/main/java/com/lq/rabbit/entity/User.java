package com.lq.rabbit.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author tianmeng
 * @date 2020/3/15
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User implements Serializable {

    private String userName;

    private String email;

}
