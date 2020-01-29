<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>

<!DOCTYPE html>
<html>
<head>
<meta charset= "UTF-8">
<link rel="stylesheet" type="text/css" href="./css/table.css">
<link rel="stylesheet" type="text/css" href="./css/style.css">


<title>Login</title>
</head>
<body>
	<!-- ヘッダー -->
	<jsp:include page="header.jsp" />

	<div id="main">
		<h1>ログイン画面</h1>

		<!--  ユーザIDに入力エラーがあったらエラーメッセージを表示 -->
		<s:if
			test="userIdErrorMessageList!=null && userIdErrorMessageList.size()>0">
			<div class="error">
				<div class="error-message">
					<s:iterator value="userIdErrorMessageList">
						<s:property />
						<br>
					</s:iterator>
				</div>
			</div>
		</s:if>

		<!--  パスワードに入力エラーがあったらエラーメッセージを表示 -->
		<s:if
			test="passwordErrorMessageList!=null && passwordErrorMessageList.size()>0">
			<div class="error">
				<div class="error-message">
					<s:iterator value="passwordErrorMessageList">
						<s:property />
						<br>
					</s:iterator>
				</div>
			</div>
		</s:if>

		<!-- 認証失敗した場合エラーメッセージを表示 -->
		<s:if
			test="isNotUserInfoMessage!=null && !isNotUserInfoMessage.isEmpty()">
			<div class="error">
				<div class="error-message">
					<s:property value="isNotUserInfoMessage" />
				</div>
			</div>
		</s:if>

		<s:form action="LoginAction">
			<table class="vertical-list-table">
				<tr>
					<th scope="row"><s:label value="ユーザーID" /></th>
					<!-- 仮ユーザIDが作成されていた場合 -->
					<s:if test="#session.savedUserIdFlg==true">
						<td><s:textfield name="userId" class="txt"
								value='%{#session.userId}' autocomplete="off" /></td>
					</s:if>
					<s:else>
						<td><s:textfield name="userId" class="txt-login" value='%{userId}'
								autocomplete="off" /></td>
					</s:else>
				</tr>
				<tr>
					<th scope="row"><s:label value="パスワード" /></th>
					<td><s:password name="password" class="txt-login" autocomplete="off" /></td>
				</tr>
			</table>

			<!-- ユーザID保存チェックボックス -->
			<div class="checkbox-login">
				<s:if
					test="(#session.savedUserIdFlg==true && #session.userId!=null && !#session.userId.isEmpty()) || savedUserIdFlg == true">
					<s:checkbox name="savedUserIdFlg" checked="checked" />
				</s:if>
				<s:else>
					<s:checkbox name="savedUserIdFlg" />
				</s:else>
				<s:label value="ユーザーID保存" />
				<br>
			</div>

			<!-- ログインボタン -->
			<div class="submit_btn_box">
				<s:submit value="ログイン" class="submit_btn" />
			</div>
		</s:form>

		<!-- 新規ユーザ登録ボタン -->
		<s:form action="CreateUserAction">
			<div class="submit_btn_box">
				<div class="contents_btn">
					<s:submit value="新規ユーザ登録" class="submit_btn" />
				</div>
			</div>
		</s:form>

		<!-- パスワード再設定ボタン -->
		<s:form action="ResetPasswordAction">
			<div class="submit_btn_box">
				<div class="contents_btn">
					<s:submit value="パスワード再設定" class="submit_btn" />
				</div>
			</div>
		</s:form>
	</div>

</body>
</html>
