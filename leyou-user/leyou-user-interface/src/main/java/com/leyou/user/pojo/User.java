package com.leyou.user.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Pattern;
import java.util.Date;

@Table(name = "tb_user")
@Data
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Length(min=4, max=15, message = "用户名只能在4-15位之间")
    private String username;   //用户名
    
    @JsonIgnore
    //为了安全考虑。这里对password添加了注解@JsonIgnore，这样在json序列化时，就不会把password和salt返回
    @Length(min=6, max=25, message = "密码只能在6-25位之间")
    private String password;  //密码
    
    @Pattern(regexp = "^1[35678]\\d{9}$", message = "手机号格式不正确")
    private String phone;  //电话
    private Date created;  //创建时间

}
