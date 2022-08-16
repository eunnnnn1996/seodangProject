$(function(){
	
	function getContextPath() {
		var hostIndex = location.href.indexOf( location.host ) + location.host.length;
		return location.href.substring( hostIndex, location.href.indexOf('/', hostIndex + 1) );
	}
	let contextPath = getContextPath();
	
	starpercent();
	$("#back_img").click(function(){
		let on_num = $(this).data('on_num');
		console.log(on_num);
		location.href='onclassDetail.do?on_num='+on_num;
	});
	
	function starpercent(){
		let star_5_pct=0;
		let star_4_pct=0;
		let star_3_pct=0;
		let star_2_pct=0;
		let star_1_pct=0;
		let count = $('#count').data('count');
		if(count!=0){
			star_5_pct = $('.star_5').data('value')/count*100;
			star_4_pct = $('.star_4').data('value')/count*100;
			star_3_pct = $('.star_3').data('value')/count*100;
			star_2_pct = $('.star_2').data('value')/count*100;
			star_1_pct = $('.star_1').data('value')/count*100;
		}
		$('.graph .graph-5').css('width', star_5_pct+'%');
		$('.graph .graph-4').css('width', star_4_pct+'%');
		$('.graph .graph-3').css('width', star_3_pct+'%');
		$('.graph .graph-2').css('width', star_2_pct+'%');
		$('.graph .graph-1').css('width', star_1_pct+'%');
	}
	
	$('.reply-btn').click(function(){
		initForm();
		let user_num = $(this).parents('#reviewList').attr('data-user');
		let writer_name =$(this).parents('#reviewList').attr('data-name');
		let onstar_num = $(this).parents('.review-set').attr('data-num');
		let photo_name = $(this).parents('#reviewList').attr('data-photo');
		
		
		let output  = '<div class="output-div display-flex">'
			output += '<div class="user_image">'
			if(photo_name!=null){
				output += '		<img src="imageViewUser.do?user_num='+user_num+'" >';
			}
			if(photo_name==null){
				output += '		<img src="'+contextPath +'/resources/images/face.png'+'" >';
			}
			output += '</div>'
			output += '<form id="onre_form" class="balloon" >';
			output += '		<input type="hidden" name="onstar_num" id="onstar_num" value="'+onstar_num+'">';
			output += '		<div>'+writer_name+" 강사님 답변"+'</div>';
			output += '		<textarea rows="3" cols="50" name="onre_content" id="onre_content"></textarea>';
			output += '		<div id="re_first">';
			output += '			<span class="letter-count">300/300</span>';
			output += '		</div>';
			output += '		<div id="re_second">';
			output += '			<input type="submit" class="btn" value="전송">';
			output += '			<input type="button" class="btn" value="취소" id="reset-btn">';
			output += '		</div>';
			output += '</form>';
			output += '</div>';
			
			$(this).parent('.reply').hide();
			$(this).parents('.reply-output').find('#output').append(output);
			
	});
	
	//수정폼에서 취소 버튼 클릭시 수정폼 초기화 
	$(document).on('click','#reset-btn',function(){
		initForm();
	});
	
	//댓글 수정 폼 초기화 
	function initForm(){
		$('.reply').show();
		$('.output-div ').remove();
	}
	
	
	//댓글 등록
	$(document).on('submit','#onre_form',function(event){
		if($('#onre_content').val().trim()==''){
			alert('내용을 입력하세요.');
			$('#onre_content').val('').focus();
			return false;
		}
		let onstar_num = $('#onstar_num').val();
		
		let data = $(this).serialize();
		
		
		$.ajax({
			type:'post',
			data:data,
			url:'writeOnReply.do',
			cache:false,
			timeout:30000,
			success:function(param){
				if(param.result=='logout'){
					alert('로그인해야 작성할 수 있습니다.');
				}else if(param.result=='success'){
					initForm();
					$('#reviewList').load(location.href+' #reviewList>*',"");
					$('.'+onstar_num).remove();
				}else{
					alert('등록 시 오류 발생');
				}
			},
			error:function(){
				alert('네트워크 오류 발생');
			}
		});
		//기본 이벤트 제거
		event.preventDefault();
	});
	
	//textarea에 내용 입력시 글자수 체크
	$(document).on('keyup','textarea',function(){
		//남은 글자수를 구함
		let inputLength = $(this).val().length;
		
		if(inputLength>300){//300자를 넘어선 경우
			$(this).val($(this).val().substring(0,300));
		}else{//300자 이하인 경우
			let remain = 300 - inputLength;
			remain += '/300';
			$('#onre_form .letter-count').text(remain);
			//수정폼 글자수
			$('#monre_form .letter-count').text(remain);

		}
	});
	//팝업창  
	let num;
	$("#popup01").css("display","none"); 
	
    $(".openPopup").on("click", function(event) {  //팝업오픈 버튼 누르면
    	$("#popup01").show();   //팝업 오픈
    	$("body").append('<div class="backon"></div>'); //뒷배경 생성
		$('body').css('overflow', 'hidden');
		num = $(this).attr('data-num');
    });
    
    $("body").on("click", function(event) { 
        if(event.target.className == 'backon'){
            $("#popup01").hide(); //뒷배경 클릭시 팝업 삭제
            $(".backon").hide();
			$('body').css('overflow', 'auto');
			num='';
        }
    });
 	$('.popup-close').click(function(){
		$("#popup01").hide(); //close버튼 클릭시 팝업 삭제
        $(".backon").hide();
		$('body').css('overflow', 'auto');
		num='';
	});
	
	//삭제 버튼 클릭 후 alert창
	 $('#delete-reply').click(function(){
		let choice = confirm('정말 삭제하시겠습니까?');
		if(choice){
			$.ajax({
			type:'post',
			data:{num:num},
			url:'deleteOnReview.do',
			cache:false,
			timeout:30000,
			success:function(param){
				if(param.result=='logout'){
					alert('로그인해야 삭제할 수 있습니다.');
				}else if(param.result=='success'){
					 $("#popup01").hide(); //뒷배경 클릭시 팝업 삭제
           			 $(".backon").hide();
					location.reload();
				}else{
					alert('등록 시 오류 발생');
				}
			},
			error:function(){
				alert('네트워크 오류 발생');
			}
		});
		}
	});
	
	//댓글 수정 버튼 클릭
	$(document).on('click','#modify-btn',function(){
		initForm();
		let user_num = $(this).parents('#reviewList').attr('data-user');
		let writer_name =$(this).parents('#reviewList').attr('data-name');
		let onstar_num = $(this).parents('.review-set').attr('data-num');
		let photo_name = $(this).parents('#reviewList').attr('data-photo');
		let onre_num = $(this).parents('#'+onstar_num).attr('data-onre_num');
		let onre_content = $(this).parents('.balloon').find('.re-content').attr('data-re_content');
		
		
		let output  = '<div class="output-div display-flex">'
			output += '<div class="user_image">'
			if(photo_name!=null){
				output += '		<img src="imageViewUser.do?user_num='+user_num+'" >';
			}
			if(photo_name==null){
				output += '		<img src="'+contextPath +'/resources/images/face.png'+'" >';
			}
			output += '</div>'
			output += '<form id="monre_form" class="balloon" >';
			output += '		<input type="hidden" name="onstar_num" id="onstar_num" value="'+onstar_num+'">';
			output += '		<input type="hidden" name="onre_num" id="onre_num" value="'+onre_num+'">';
			output += '		<div>'+writer_name+" 강사님 답변"+'</div>';
			output += '		<textarea rows="3" cols="50" name="onre_content" id="onre_content">'+onre_content+'</textarea>';
			output += '		<div id="re_first">';
			output += '			<span class="letter-count">300/300</span>';
			output += '		</div>';
			output += '		<div id="re_second">';
			output += '			<input type="submit" class="btn" value="전송">';
			output += '			<input type="button" class="btn" value="취소" id="mreset-btn">';
			output += '		</div>';
			output += '</form>';
			output += '</div>';
			
			$('#'+onstar_num).hide();
			$(this).parents('.review-set').find('.reply-output').append(output);
			
	});
	//수정폼에서 취소 버튼 클릭시 수정폼 초기화 
	$(document).on('click','#mreset-btn',function(){
		let onstar_num = $(this).parents('.review-set').attr('data-num');
		$('#'+onstar_num).show();
		initForm();
	});
	
	//댓글 수정 
	$(document).on('submit','#monre_form',function(event){
		if($('#onre_content').val().trim()==''){
			alert('내용을 입력하세요.');
			$('#onre_content').val('').focus();
			return false;
		}
		let onstar_num = $('#onstar_num').val();
		
		let data = $(this).serialize();
		
		
		$.ajax({
			type:'post',
			data:data,
			url:'updateOnReply.do',
			cache:false,
			timeout:30000,
			success:function(param){
				if(param.result=='logout'){
					alert('로그인해야 작성할 수 있습니다.');
				}else if(param.result=='success'){
					initForm();
					$('#'+onstar_num).load(location.href+' #'+onstar_num+'>*',"");
					$('#'+onstar_num).css('display',"block");
				}else{
					alert('등록 시 오류 발생');
				}
			},
			error:function(){
				alert('네트워크 오류 발생');
			}
		});
		//기본 이벤트 제거
		event.preventDefault();
	});
	
	//댓글 삭제
	$(document).on('click','#delete-btn',function(event){
		let onstar_num = $(this).parents('.review-set').attr('data-num');
		let onre_num = $(this).parents('#'+onstar_num).attr('data-onre_num');
		let choice = confirm('정말 삭제하시겠습니까?삭제 후 복구 불가능합니다.');
		if(choice){
			$.ajax({
			type:'post',
			data:{onre_num:onre_num},
			url:'deleteOnReply.do',
			cache:false,
			timeout:30000,
			success:function(param){
				if(param.result=='logout'){
					alert('로그인해야 삭제할 수 있습니다.');
				}else if(param.result=='success'){
					location.reload();
				}else{
					alert('등록 시 오류 발생');
				}
			},
			error:function(){
				alert('네트워크 오류 발생');
			}
		});
		}
	});
});