package com.leyou.auth;

import com.leyou.auth.entity.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.auth.utils.RsaUtils;
import org.junit.Before;
import org.junit.Test;

import java.security.PrivateKey;
import java.security.PublicKey;

public class JwtTest {
    
//    private static final String pubKeyPath = "F:\\leyouProject\\screatKey\\rsa\\rsa.pub"; //公钥
//
//    private static final String priKeyPath = "F:\\leyouProject\\screatKey\\rsa\\rsa.pri"; //私钥
//
//    private PublicKey publicKey;
//
//    private PrivateKey privateKey;
//
//    /**
//     * 生成公钥和私钥
//     * @throws Exception
//     */
//    @Test
//    public void testRsa() throws Exception {
//        RsaUtils.generateKey(pubKeyPath, priKeyPath, "234");
//    }
//
//    @Before
//    public void testGetRsa() throws Exception {
//        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
//        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
//    }
//
//    @Test
//    public void testGenerateToken() throws Exception {
//        // 生成token
//        String token = JwtUtils.generateToken(new UserInfo(20L, "jack"), privateKey, 5);
//        System.out.println("token = " + token);
//    }
//
//    @Test
//    public void testParseToken() throws Exception {
//        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6MjAsInVzZXJuYW1lIjoiamFjayIsImV4cCI6MTU2NzIzNzgxNn0.e3NudxbdmimH-C-MPXjsqGT_JFsn8eJqt7MT6UeOcpeSHeAj7eIDIgtKe7z8SbSxt4jO_zg7UBDMLBQgpcEnoSFyxiR1LqqcN59FKbTzy9Rgu4TzFYpcA7ZekyunQFLGwH_IbWATwxQugUyMfdHq6PcONAE_2LidHgWRk3fi7iw";
//
//        // 解析token
//        UserInfo user = JwtUtils.getInfoFromToken(token, publicKey);
//        System.out.println("id: " + user.getId());
//        System.out.println("userName: " + user.getUsername());
//    }
//
}
