package com.clone.BookMyShow.service;

import com.clone.BookMyShow.dto.SignupRequest;
import com.clone.BookMyShow.dto.UserResponse;

public interface UserService {
    UserResponse registerUser(SignupRequest signupRequest);
}
