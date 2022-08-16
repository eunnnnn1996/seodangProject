package kr.spring.user.service;

import java.util.List;
import java.util.Map;

import kr.spring.user.vo.UserVO;

public interface UserService {
	//사용자
	public void insertUser(UserVO user);
	public UserVO selectCheckUser(String id);
	public UserVO selectUser(Integer user_num);
	public void updateUser(UserVO user);
	public void updatePassword(UserVO user);
	public void deleteUser(Integer user_num);
	public void updateProfile(UserVO user);
	
	//관리자
	public List<UserVO> selectList(Map<String,Object> map);
	public int selectRowCount(Map<String,Object> map);
	public void updateByAdmin(UserVO user);
}
