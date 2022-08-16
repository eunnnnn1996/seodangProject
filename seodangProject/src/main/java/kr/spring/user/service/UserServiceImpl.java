package kr.spring.user.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.spring.user.dao.UserMapper;
import kr.spring.user.vo.UserVO;

@Service
@Transactional
public class UserServiceImpl implements UserService{

	@Autowired
	private UserMapper userMapper;
	


	@Override
	public void insertUser(UserVO user) {
		user.setUser_num(userMapper.selectUser_num());
		userMapper.insertUser(user);
		userMapper.insertUser_detail(user);
	}

	@Override
	public UserVO selectCheckUser(String id) {
		return userMapper.selectCheckUser(id);
	}

	@Override
	public UserVO selectUser(Integer user_num) {
		return userMapper.selectUser(user_num);
	}

	@Override
	public void updateUser(UserVO user) {
		userMapper.updateUser(user);
		
	}

	@Override
	public void updatePassword(UserVO user) {
		userMapper.updatePassword(user);
		
	}

	@Override
	public void deleteUser(Integer user_num) {
		userMapper.deleteUser(user_num);
		userMapper.deleteUser_detail(user_num);
	}


	@Override
	public void updateProfile(UserVO user) {
		userMapper.updateProfile(user);
	}

	@Override
	public List<UserVO> selectList(Map<String, Object> map) {
		return userMapper.selectList(map);
	}

	@Override
	public int selectRowCount(Map<String, Object> map) {
		return userMapper.selectRowCount(map);
	}

	@Override
	public void updateByAdmin(UserVO user) {
		userMapper.updateByAdmin(user);
		userMapper.updateDetailByAdmin(user);
	}


}
