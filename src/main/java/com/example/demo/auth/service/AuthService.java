package com.example.demo.auth.service;

import com.example.demo.auth.dto.SignupRequest;
import com.example.demo.auth.dto.SignupResponse;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

	private final AtomicLong sequence = new AtomicLong(1);
	private final Map<String, SignupResponse> membersByEmail = new ConcurrentHashMap<>();

	public SignupResponse signup(SignupRequest request) {
		if (!request.password().equals(request.passwordConfirm())) {
			throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
		}

		if (membersByEmail.containsKey(request.email())) {
			throw new IllegalArgumentException("이미 가입된 이메일입니다.");
		}

		SignupResponse response = new SignupResponse(
			sequence.getAndIncrement(),
			request.name(),
			request.email(),
			request.phone()
		);
		membersByEmail.put(request.email(), response);

		return response;
	}
}
