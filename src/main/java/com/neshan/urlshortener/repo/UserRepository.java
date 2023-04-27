package com.neshan.urlshortener.repo;

import com.neshan.urlshortener.entity.User;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, String> {

  Optional<User> findByUsername(String username);
}
