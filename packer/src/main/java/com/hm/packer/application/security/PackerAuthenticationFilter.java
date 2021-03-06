package com.hm.packer.application.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hm.packer.application.security.token.PreLoginAuthenticationToken;
import com.hm.packer.application.security.token.PreLicenseKeyAuthenticationToken;
import com.hm.packer.application.security.token.Certified;
import com.hm.packer.model.dto.EngineerAuthDto;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.FilterChain;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class PackerAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private ObjectMapper mapper;

    protected PackerAuthenticationFilter(RequestMatcher requiresAuthenticationRequestMatcher) {
        super(requiresAuthenticationRequestMatcher);
        mapper = new ObjectMapper();
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws AuthenticationException, IOException, ServletException {
       String requestURI = httpServletRequest.getRequestURI();

       return super.getAuthenticationManager().authenticate(
               requestURI.equals("/engineer/login/auth") ?
                        new PreLoginAuthenticationToken(EngineerAuthDto.builder().id(httpServletRequest.getParameter("id"))
                        .password(httpServletRequest.getParameter("password")).build()) :
                        new PreLicenseKeyAuthenticationToken(httpServletRequest.getParameter("key")));
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        SecurityContext contextHolder = SecurityContextHolder.createEmptyContext();
        Certified token = (Certified) authResult;

        System.out.println("USER LOGIN SUCCESS");
        System.out.println("ID : " + token.getEngineer().getId());
        System.out.println("NAME : " + token.getEngineer().getName());
        System.out.println("Email : " + token.getEngineer().getEmail());
        System.out.println("ENGINEER key : " + token.getEngineer().getKey());
        System.out.println("LICENSE KEY : " + token.getLicenseKey());



        contextHolder.setAuthentication(token);
        SecurityContextHolder.setContext(contextHolder);

        response.sendRedirect("/install");
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        failed.printStackTrace();

        request.setAttribute("errorMessage", failed.getMessage());
        RequestDispatcher dispatcherServlet = request.getRequestDispatcher("/errorMessage");
        dispatcherServlet.forward(request,response);
    }
}
