package kr.spring.onclass.controller;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import kr.spring.myclass.service.MyclassService;
import kr.spring.offclass.vo.OffclassVO;
import kr.spring.offclass.vo.OffstarVO;
import kr.spring.onclass.service.OnclassService;
import kr.spring.onclass.vo.OnclassVO;
import kr.spring.onclass.vo.OnstarVO;
import kr.spring.onclass.vo.UploadFileVO;
import kr.spring.user.controller.UserController;
import kr.spring.user.service.UserService;
import kr.spring.user.vo.UserVO;
import kr.spring.util.PagingUtil;

@Controller
public class OnclassController {
	public static final Logger logger = LoggerFactory.getLogger(UserController.class);
	@Autowired
	private OnclassService onclassService;
	@Autowired
	private UserService userService;
	@Autowired
	private MyclassService myclassService;
	
	@ModelAttribute
	public OnclassVO initCommand() {
		return new OnclassVO();
	}
	
	//온라인 클래스 목록
	@RequestMapping("/onclass/onclassList.do")
	public ModelAndView process(
			@RequestParam(value="pageNum",defaultValue="1")
			int currentPage,
			@RequestParam(value="keyfield",defaultValue="")
			String keyfield,
			@RequestParam(value="keyword",defaultValue="")
			String keyword, @RequestParam(value="category_num",defaultValue="0") int category_num,
			@RequestParam(value="sort",defaultValue="0") int sort
			) {
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("keyfield", keyfield);
		map.put("keyword", keyword);
		map.put("category_num", category_num);
		map.put("sort", sort);
		
		int count = onclassService.selectRowCount(map);
		
		PagingUtil page = new PagingUtil(keyfield,keyword,
                currentPage,count,8,10,"onclassList.do");

		map.put("start",page.getStartCount());
		map.put("end", page.getEndCount());
		
		List<OnclassVO> list = null;
			if(count > 0) {
				list = onclassService.selectList(map);
				for(int i=0;i<list.size();i++) {
					OnclassVO onclassVO = list.get(i);
					int like_count = onclassService.selectLikeCount(onclassVO.getOn_num());
					onclassVO.setLike_count(like_count);
					onclassVO.setRating_percent(onclassVO.getRating()*20);
				}
			}

		ModelAndView mav = new ModelAndView();
		mav.setViewName("onclassList");
		mav.addObject("count", count);
		mav.addObject("list",list);
		mav.addObject("sort",sort);
		mav.addObject("category_num",category_num);
		mav.addObject("pagingHtml", page.getPagingHtml());
		
		return mav;
	}
	
	//수업 등록
	@GetMapping("/onclass/onclassInsert.do")
	public String insertForm() {
		return "onclassInsert";
	}
	
	@PostMapping("/onclass/onclassInsert.do")
	public String insert(@Valid OnclassVO onclassVO,BindingResult result,HttpSession session,
			MultipartHttpServletRequest mhsq) throws IllegalStateException, IOException{

		Integer user_num = (Integer)session.getAttribute("session_user_num");
		onclassVO.setUser_num(user_num);
		
		int on_num = onclassService.currSelect();
		
		String uploadFolder = "/resources/image_upload";
		String realFolder = session.getServletContext().getRealPath(uploadFolder);
		
		
        File dir = new File(realFolder);
        if (!dir.isDirectory()) {
            dir.mkdirs();
        }
        
        String mimage = null; 
        //넘어온 파일을 리스트로 저장
        List<MultipartFile> mf = mhsq.getFiles("uploadFile");
        if (mf.size() == 1 && mf.get(0).getOriginalFilename().equals("")) {       
        } else {
            for (int i = 0; i < mf.size(); i++) {
                //파일 중복명 처리
                String genId = UUID.randomUUID().toString();
                //본래 파일명
                String originalfileName = mf.get(i).getOriginalFilename();                
                String extension = StringUtils.getFilenameExtension(originalfileName);
                //저장되는 파일 이름 
                String saveFileName = genId + "." + extension;
                //저장 될 파일 경로
                String savePath = realFolder +"/"+ saveFileName;
                //파일 사이즈
                long fileSize = mf.get(i).getSize(); 
                //파일 저장
                mf.get(i).transferTo(new File(savePath)); 
					
				if(i == 0) {mimage = saveFileName;}
             
				
				onclassService.fileUpload(originalfileName, saveFileName, fileSize, on_num);
				 

            }
        }
        
        
		if(result.hasErrors()) {
			return insertForm();
		}
		
		onclassVO.setOn_num(on_num);
		onclassVO.setMimage(mimage);
		onclassService.insertOnclass(onclassVO);
    
		return "redirect:/onclass/onclassList.do";
	}



	@GetMapping("/onclass/onclassModify.do")
	public String updateForm(@RequestParam int on_num, Model model) {
		OnclassVO onclassVO = onclassService.selectOnclass(on_num);
		model.addAttribute("onclassVO",onclassVO);
		//다중이미지 출력
		List<UploadFileVO> list = onclassService.selectFile(on_num);
		
		model.addAttribute("list",list);
		
		return "onclassModify";		
	}
	@PostMapping("/onclass/onclassModify.do")
	public String update(@Valid OnclassVO onclassVO,BindingResult result,MultipartHttpServletRequest mhsq,
							HttpSession session,HttpServletRequest request,Model model) throws IllegalStateException, IOException {

		int on_num = onclassVO.getOn_num();
		
		//수정할때 전에 올린 이미지 삭제
		onclassService.deleteFile(on_num);
		
		String uploadFolder = "/resources/image_upload";
		String realFolder = session.getServletContext().getRealPath(uploadFolder);
        File dir = new File(realFolder);
        if (!dir.isDirectory()) {
            dir.mkdirs();
        }
        
        //넘어온 파일을 리스트로 저장
        
        String mimage = null; //메인 이미지 저장 
        
        List<MultipartFile> mf = mhsq.getFiles("uploadFile");
        if (mf.size() == 1 && mf.get(0).getOriginalFilename().equals("")) {       
        } else {
            for (int i = 0; i < mf.size(); i++) {
                //파일 중복명 처리
                String genId = UUID.randomUUID().toString();
                //본래 파일명
                String originalfileName = mf.get(i).getOriginalFilename();                
                String extension = StringUtils.getFilenameExtension(originalfileName);
                //저장되는 파일 이름 
                String saveFileName = genId + "." + extension;
                //저장 될 파일 경로
                String savePath = realFolder +"/"+ saveFileName;
                //파일 사이즈
                long fileSize = mf.get(i).getSize(); 
                //파일 저장
                mf.get(i).transferTo(new File(savePath));
                
                if(i == 0) {mimage = saveFileName;} //for 첫번째 이미지 변수 저장
                
                onclassService.fileUpload(originalfileName, saveFileName, fileSize, on_num);
				
				 
            }
        }
		
        List<UploadFileVO> list = onclassService.selectFile(on_num);
		
		if(result.hasErrors()) {
			return "onclassModify";
		}
		onclassVO.setMimage(mimage);
		onclassService.updateOnclass(onclassVO);

		//다중파일 띄우기
		model.addAttribute("list",list);
		model.addAttribute("message", "글 수정 완료!!");
		model.addAttribute("url", request.getContextPath() + "/onclass/onclassList.do");
		
		
		
		return "common/resultView";
	}
	
	@GetMapping("/onclass/onclassDelete.do")
	public String delete(int on_num,OnclassVO onV) {
		
		
		onV.setOn_num(on_num);
		return "onclassDelete";
	}
	
	@PostMapping("/onclass/onclassDelete.do")
	public String deleteCheck(UserVO uvo,OnclassVO vo,String deletePasswd,HttpSession session,HttpServletRequest request,Model model) {
		
		
		Integer user_num = (Integer)session.getAttribute("session_user_num");
		int on_num = vo.getOn_num();
		UserVO zvo = userService.selectUser(user_num);
		if(deletePasswd.equals(zvo.getPasswd())) {
			if(myclassService.peopleCount(on_num) == 0) {
			onclassService.deleteOnclass(on_num);
			model.addAttribute("message", "게시물 삭제 완료");
			model.addAttribute("url", request.getContextPath() + "/onclass/onclassList.do");
			}else if(myclassService.peopleCount(on_num) > 0) {
				model.addAttribute("message", "수강중인 학생이 존재 삭제불가");
				model.addAttribute("url", request.getContextPath() + "/onclass/onclassDetail.do?on_num="+on_num);
			}
		}else {
			model.addAttribute("message", "비밀번호 불일치");
			model.addAttribute("url", request.getContextPath() + "/onclass/onclassDetail.do?on_num="+on_num);
		}
		return "common/resultView";
	}
	
	@GetMapping("/onclass/onclassDetail.do")
	public ModelAndView detailForm(HttpSession session,int on_num) {
		onclassService.updateHit(on_num); //조회수
		Map<String, Object> map = new HashMap<String, Object>();
		//다중업로드 파일 가져오기 시작
		List<UploadFileVO> uplist = onclassService.selectFile(on_num);
		//다중업로드 파일 가져오기 끝
		
		OnclassVO oVO = onclassService.selectOnclass(on_num);
	
		ModelAndView mav = new ModelAndView();
		map.put("on_num",on_num);
		//리뷰 개수
		int review_count = onclassService.selectRowReviewCount(map);
		//별점 점수
		float rating = 0;
		if(review_count>0) {
			rating= onclassService.selectReviewRating(on_num);
		}
		
		//후기 목록 받아오기
		Map<String, Object> map2 = new HashMap<String, Object>();
		map2.put("on_num", on_num);
		map2.put("rownum",6);
		List<OnstarVO> list2 = onclassService.selectListOnReview(map2);
		//별점 점수 세팅하기
		for(int i=0;i<list2.size();i++) {
			OnstarVO onstarVO = list2.get(i);
			onstarVO.setRating_percent(onstarVO.getRating()*20);
		}
		
		//프로필 뿌림
		mav.setViewName("onclassDetail");
		mav.addObject("onclass",oVO);		
		mav.addObject("peopleCount", myclassService.peopleCount(on_num));
		mav.addObject("rating",rating);
		mav.addObject("review_count",review_count);
		mav.addObject("list2",list2);
		//다중 업로드
		mav.addObject("uplist",uplist);
		for(int i=0; i<4;i++) {
			mav.addObject("upfile"+i,uplist.get(i));
		}
			
		return mav;
	}
	
		//온라인 클래스 후기 작성 폼 
		@GetMapping("/onclass/onclassReview.do")
		public String reviewForm(OnstarVO onstarVO,int on_num,HttpSession session,Model model) {
			
			OnclassVO onclassVO = onclassService.selectOnclass(on_num);
			onstarVO.setOn_name(onclassVO.getOn_name());
			
			Integer user_num = (Integer)session.getAttribute("session_user_num");

			//작성했는지 확인을 하기 위해
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("on_num",on_num);
			map.put("user_num",user_num);
			//작성했는지 확인하기 위해
			OnstarVO onstarVO2 = onclassService.selectOnReview(map);
			if(onstarVO2!=null) {
				model.addAttribute("onstarVO",onstarVO2);
				return "redirect:/onclass/onclassReviewUpdate.do?on_num="+on_num;
			}
			
			return "onclassReview";
		}
		
		//온라인 클래스 후기 작성
		@PostMapping("/onclass/onclassReview.do")
		public String reviewSubmit(@Valid OnstarVO onstarVO,BindingResult result,HttpSession session) {
			int on_num = onstarVO.getOn_num();
			
			if(result.hasErrors()) {
				return "onclassReview";
			}
			Integer user_num = (Integer)session.getAttribute("session_user_num");
			//회원번호 세팅
			onstarVO.setUser_num(user_num);
			onclassService.insertOnReview(onstarVO);
			
			return "redirect:/onclass/onclassDetail.do?on_num="+on_num;
		}
		
		//온라인 클래스 후기 목록
		@RequestMapping("/onclass/onclassReviewList.do")
		public ModelAndView processReview(@RequestParam int on_num,HttpSession session,@RequestParam(value="sort",defaultValue="0") int sort) {
			
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("on_num",on_num);
			map.put("sort",sort);
			int count = onclassService.selectRowReviewCount(map);
			OnclassVO onclassVO=onclassService.selectOnclass(on_num);
			int writer_num = onclassVO.getUser_num();
			String photo_name= onclassVO.getPhoto_name();
			String writer_name= onclassVO.getName();//작성자 이름
			Integer session_user_num = (Integer)session.getAttribute("session_user_num");
			
			List<OnstarVO> list = null;
			float rating = 0;
			int star_1 = 0;
			int star_2 = 0;
			int star_3 = 0;
			int star_4 = 0;
			int star_5 = 0;
			ModelAndView mav = new ModelAndView();
			if(count>0) {
				rating= onclassService.selectReviewRating(on_num);
				list = onclassService.selectListOnReview(map);
				map.put("rating",1);
				star_1 = onclassService.selectRowReviewCount(map);
				Map<String, Object> map_2 = new HashMap<String, Object>();
				map_2.put("on_num",on_num);
				map_2.put("rating",2);
				star_2 = onclassService.selectRowReviewCount(map_2);
				Map<String, Object> map_3 = new HashMap<String, Object>();
				map_3.put("on_num",on_num);
				map_3.put("rating",3);
				star_3 = onclassService.selectRowReviewCount(map_3);
				Map<String, Object> map_4 = new HashMap<String, Object>();
				map_4.put("on_num",on_num);
				map_4.put("rating",4);
				star_4 = onclassService.selectRowReviewCount(map_4);
				Map<String, Object> map_5 = new HashMap<String, Object>();
				map_5.put("on_num",on_num);
				map_5.put("rating",5);
				star_5 = onclassService.selectRowReviewCount(map_5);
				for(int i=0;i<list.size();i++) {
					OnstarVO onstarVO = list.get(i);
					onstarVO.setRating_percent(onstarVO.getRating()*20);
				}
			}
			
			
			mav.setViewName("onclassReviewList");
			mav.addObject("list", list);
			mav.addObject("count", count);
			mav.addObject("star_1", star_1);
			mav.addObject("star_2", star_2);
			mav.addObject("star_3", star_3);
			mav.addObject("star_4", star_4);
			mav.addObject("star_5", star_5);
			mav.addObject("rating", rating);
			//글 작성자 -강사
			mav.addObject("writer_num",writer_num);
			mav.addObject("photo_name",photo_name);
			mav.addObject("writer_name",writer_name);
			if(session_user_num!=null) {
				mav.addObject("session_user_num",session_user_num);
			}
			mav.addObject("on_num",on_num);
			
			return mav;
		}
		//온라인 클래스 후기 수정폼
		@GetMapping("/onclass/onclassReviewUpdate.do")
		public String reviewformUpdate(@RequestParam int on_num,HttpSession session,Model model) {
			Map<String, Object> map = new HashMap<String, Object>();
			
			map.put("on_num", on_num);
			Integer user_num = (Integer)session.getAttribute("session_user_num");
			map.put("user_num", user_num);
			OnstarVO onstarVO = onclassService.selectOnReview(map);
			
			OnclassVO onclassVO = onclassService.selectOnclass(on_num);
			onstarVO.setOn_name(onclassVO.getOn_name());
			
			model.addAttribute("onstarVO",onstarVO);
			
			return "onclassReviewUpdate";
		}
		//온라인 클래스 후기 수정
		@PostMapping("/onclass/onclassReviewUpdate.do")
		public String reviewsubmitUpdate(@Valid OnstarVO onstarVO, BindingResult result,Model model) {

			int on_num = onstarVO.getOn_num();
			if(result.hasErrors()) {
				return "onclassReviewUpdate";
			}
			
			onclassService.updateOnReview(onstarVO);
			
			model.addAttribute("onstarVO",onstarVO);
			
			return "redirect:/onclass/onclassReviewList.do?on_num="+on_num;
		}
}

















