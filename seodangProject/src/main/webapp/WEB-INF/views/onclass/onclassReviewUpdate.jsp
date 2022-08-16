<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/offclass.css">
<!-- 중앙 컨텐츠 시작 -->
<script src="${pageContext.request.contextPath}/resources/js/jquery-3.6.0.min.js"></script>
<div class="review">
	<div class="back-title">
		<span class="backspace">
		<img id="back_img" src="${pageContext.request.contextPath}/resources/images/back_arrow.png">
		</span>
		<div class="title">
			<h3>후기 수정</h3>
		</div>
	</div>
	<div class="reviewForm">
	<div class="align-center">
		<img class="review_img" src="${pageContext.request.contextPath}/resources/image_upload/${onstarVO.mimage}">
		<div>${onstarVO.on_name }</div>
	</div>
	<form:form action="onclassReviewUpdate.do" modelAttribute="onstarVO" id="review_form">
	<form:errors element="div" cssClass="error-color"/>
	<form:hidden path="onstar_num"/>
	<form:hidden path="on_num"/>
	<ul>
		<li class="starpoint_box">
			<div class="starpoint">
			<input type="radio" id="1-stars" name="rating" value="5"<c:if test="${onstarVO.rating==5 }">checked</c:if>/>
			<label for="1-stars" class="star pr-4">★</label>
			<input type="radio" id="2-stars" name="rating" value="4"<c:if test="${onstarVO.rating==4 }">checked</c:if>/>
			<label for="2-stars" class="star">★</label>
			<input type="radio" id="3-stars" name="rating" value="3"<c:if test="${onstarVO.rating==3 }">checked</c:if>/>
			<label for="3-stars" class="star">★</label>
			<input type="radio" id="4-stars" name="rating" value="2"<c:if test="${onstarVO.rating==2 }">checked</c:if>/>
			<label for="4-stars" class="star">★</label>
			<input type="radio" id="5-star" name="rating" value="1" <c:if test="${onstarVO.rating==1 }">checked</c:if>/>
			<label for="5-star" class="star">★</label>
			</div>
			<div id="star_review"></div>
			<form:errors path="rating" cssClass="error-color"/>
		</li>
		<li>
			<div class="textarea-label">
			<form:label path="text">후기 작성</form:label><br>
			</div>
			<form:textarea id="review_text" path="text" cols="30" rows="5"/>
			<div class="text-count align-right">300/300</div>
			<form:errors path="text" cssClass="error-color"/>
		</li>
		<li>
			<form:button class="btn detail-replybtn">후기 작성완료</form:button>
		</li>
	</ul>
	</form:form>
	</div>
</div>