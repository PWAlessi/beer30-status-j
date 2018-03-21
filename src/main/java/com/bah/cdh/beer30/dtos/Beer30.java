package com.bah.cdh.beer30.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Beer30 {
    private Integer id;
    private String name;
    private String state;
    private String text;
    private Date updatedAt;
}
