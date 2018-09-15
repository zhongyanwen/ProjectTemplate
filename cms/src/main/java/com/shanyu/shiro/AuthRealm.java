package com.shanyu.shiro;

import com.shanyu.entity.TbSysUser;
import com.shanyu.service.serviceImpl.TbSysUserServiceImpl;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.security.MessageDigest;

public class AuthRealm extends AuthorizingRealm{
    @Autowired
    private TbSysUserServiceImpl tbSysUserService;
    
    //认证.登录
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
		// 登陆的主要信息: 可以是一个实体类的对象, 但该实体类的对象一定是根据 token 的 username 查询得到的.
		Object principal = token.getPrincipal();
		// 认证信息: 从数据库中查询出来的信息. 密码的比对交给 shiro 去进行比较
		TbSysUser sysUser=tbSysUserService.findByName(token.getPrincipal().toString());
		String credentials = null;
		if (sysUser != null) {
            // 用户名不存在抛出异常
            SimpleAuthenticationInfo info = new SimpleAuthenticationInfo(principal, sysUser.getPassword(),getName());
            return info;
        }
        return null;

    }
    //授权
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principal) {
//        User user=(User) principal.fromRealm(this.getClass().getName()).iterator().next();//获取session中的用户
//        List<String> permissions=new ArrayList<>();
//        Set<Role> roles = user.getRoles();
//        if(roles.size()>0) {
//            for(Role role : roles) {
//                Set<Module> modules = role.getModules();
//                if(modules.size()>0) {
//                    for(Module module : modules) {
//                        permissions.add(module.getMname());
//                    }
//                }
//            }
//        }
    	SimpleAuthorizationInfo info=new SimpleAuthorizationInfo();
//        info.addStringPermissions(permissions);//将权限放入shiro中.
        return info;
    }

}
