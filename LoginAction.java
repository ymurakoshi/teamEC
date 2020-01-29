package com.internousdev.espresso.action;

import java.util.List;
import java.util.Map;

import org.apache.struts2.interceptor.SessionAware;

import com.internousdev.espresso.dao.CartInfoDAO; //カート情報処理クラス
import com.internousdev.espresso.dao.UserInfoDAO; //ユーザ情報処理クラス
import com.internousdev.espresso.dto.CartInfoDTO; //カート情報格納クラス
import com.internousdev.espresso.util.InputChecker; //入力チェック処理クラス
import com.opensymphony.xwork2.ActionSupport;

public class LoginAction extends ActionSupport implements SessionAware {

	private String userId; 							//ユーザID
	private String password; 						//パスワード
	private boolean savedUserIdFlg; 				//ユーザID保存フラグ
	private List<String> userIdErrorMessageList; 	//ユーザID入力エラーリスト
	private List<String> passwordErrorMessageList;	//パスワード入力エラーリスト
	private String isNotUserInfoMessage; 			//ログイン情報不一致エラーメッセージ
	private List<CartInfoDTO> cartInfoDTOList; 		//カート格納リスト
	private int totalPrice;							//購入合計金額
	private Map<String, Object> session;

	public String execute() {
		String result = ERROR;
		UserInfoDAO userInfoDAO = new UserInfoDAO();

		//(ログアウト時に代入された)ユーザID保存の値を削除
		session.remove("savedUserIdFlg");

		//ユーザ情報入力完了画面から遷移の場合
		if (session.containsKey("createUserFlg")
				&& Integer.parseInt(session.get("createUserFlg").toString()) == 1) {
			//ユーザ登録時に保持されたユーザIDを取得
			userId = session.get("userIdForCreateUser").toString();
			//セッションからユーザ登録時に保持したユーザIDとユーザ情報入力完了フラグを削除
			session.remove("userIdForCreateUser");
			session.remove("createUserFlg");

		} else {
			//ログイン情報入力チェック処理
			InputChecker inputchecker = new InputChecker();
			//ユーザID(userId)の文字数、半角英字、半角数字をチェック
			userIdErrorMessageList = inputchecker.doCheck("ユーザID", userId, 1, 8, true, false, false, true, false,
					false);
			//パスワード(password)の文字数、半角英字、半角数字をチェック
			passwordErrorMessageList = inputchecker.doCheck("パスワード", password, 1, 16, true, false, false, true, false,
					false);

			//ユーザIDかつパスワードの入力チェックエラーが1つ以上あったら
			if (userIdErrorMessageList.size() > 0
					|| passwordErrorMessageList.size() > 0) {
				session.put("loginFlg", 0); //セッションのログインフラグに0を保持
				return result; //ログイン画面(login.jsp)へ遷移
			}

			//認証処理（DBの会員情報テーブルに、一致するユーザとパスワードが存在しているか）
			if (!userInfoDAO.isExistsUserInfo(userId, password)) { //ログイン失敗の場合（対象のユーザ情報が登録されていなかった場合）
				isNotUserInfoMessage = "ユーザIDまたはパスワードが異なります。"; //ログイン情報不一致のエラーメッセージを表示
				return result; //ログイン画面(login.jsp)へ遷移
			}
		}

		//セッションタイムアウト(ログインしていないためuserIdは見ない)処理
		if (!session.containsKey("tempId")) { //セッションに仮ユーザIDが保持されていない場合
			return "sessionTimeout"; //セッションエラー画面(sessionerror.jsp)へ遷移
		}

		CartInfoDAO cartInfoDAO = new CartInfoDAO();
		//カート情報をユーザに紐付ける処理
		String tempId = session.get("tempId").toString();
		List<CartInfoDTO> cartInfoDTOListForTempUser = cartInfoDAO.getCartInfoDTOList(tempId);
		if (cartInfoDTOListForTempUser != null && cartInfoDTOListForTempUser.size() > 0) { //仮ユーザに紐づくカート情報が1つ以上入っている場合
			boolean cartresult = changeCartInfo(cartInfoDTOListForTempUser, tempId); //仮ユーザカート情報の紐付けを行う
			if (!cartresult) { //カート情報の紐付けが失敗した場合
				return "DBError"; //システムエラー画面(System.error.jsp)へ遷移
			}
		}

		//ユーザ情報(ユーザID、ログインフラグ、ユーザID保持がtrueの場合ユーザID保持）をsessionに保持し、tempIdを削除する
		session.put("userId", userId); //ユーザIDをセッションへ追加
		session.put("loginFlg", 1); //ログインフラグ(1)をセッションへ追加
		if (savedUserIdFlg) { //ユーザID保持にチェックが入っていたら
			session.put("savedUserIdFlg", true); //ユーザID保持フラグ(true)をセッションへ追加
		}
		session.remove("tempId"); //仮ユーザIDはセッションから削除

		//次の遷移先を設定
		if (session.containsKey("cartFlg") //セッションにカートフラグが保持されていた場合
				&& Integer.parseInt(session.get("cartFlg").toString()) == 1) {
			//カート画面に表示する情報を取得
			session.remove("cartFlg"); //セッションのカートフラグを削除
			cartInfoDTOList = cartInfoDAO.getCartInfoDTOList(userId); //カート情報を更新
			totalPrice = cartInfoDAO.getTotalPrice(userId); //合計金額を更新
			result = "cart"; //カート画面(cart.jsp)へ遷移
		} else {
			result = SUCCESS;
		}
		return result; //ホーム画面(home.jsp)へ遷移
	}

	/**
	 * DBのカート情報を更新、作成する
	 * 	@param cartInfoDTOListForTempUser 仮ユーザに紐づくカート情報
	 * 	@param tempId 仮ユーザID
	 */

	private boolean changeCartInfo(List<CartInfoDTO> cartInfoDTOListForTempUser, String tempId) {
		int count = 0;
		CartInfoDAO cartInfoDAO = new CartInfoDAO();
		boolean result = false;

		for (CartInfoDTO dto : cartInfoDTOListForTempUser) {
			//処理対象のカート情報とDBのカート情報に、ユーザIDに紐づく同じ商品IDのカート情報が存在する場合
			if (cartInfoDAO.isExistsCartInfo(userId, dto.getProductId())) {
				//対象のカート情報の個数を、ユーザIDのカート情報の個数に足す
				count += cartInfoDAO.updateProductCount(userId, dto.getProductId(), dto.getProductCount());
				//カート情報から、仮ユーザID(tempId)に紐づくカート情報を削除する
				cartInfoDAO.delete(String.valueOf(dto.getProductId()), tempId);
			} else {
				//同じ商品IDのカート情報が存在しない場合、対象のカート情報のユーザIDをログインするユーザIDに更新する
				count += cartInfoDAO.linkToUserId(tempId, userId, dto.getProductId());
			}
		}
		//変数countに格納された数がcartInfoDTOListForTempUserの結果の数と等しければ
		if (count == cartInfoDTOListForTempUser.size()) {
			result = true;
		}
		return result; //セッションへ情報を追加

	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isSavedUserIdFlg() {
		return savedUserIdFlg;
	}

	public void setSavedUserIdFlg(boolean savedUserIdFlg) {
		this.savedUserIdFlg = savedUserIdFlg;
	}

	public List<String> getUserIdErrorMessageList() {
		return userIdErrorMessageList;
	}

	public void setUserIdErrorMessageList(List<String> userIdErrorMessageList) {
		this.userIdErrorMessageList = userIdErrorMessageList;
	}

	public List<String> getPasswordErrorMessageList() {
		return passwordErrorMessageList;
	}

	public void setPasswordErrorMessageList(List<String> passwordErrorMessageList) {
		this.passwordErrorMessageList = passwordErrorMessageList;
	}

	public String getIsNotUserInfoMessage() {
		return isNotUserInfoMessage;
	}

	public void setIsNotUserInfoMessage(String isNotUserInfoMessage) {
		this.isNotUserInfoMessage = isNotUserInfoMessage;
	}

	public List<CartInfoDTO> getCartInfoDTOList() {
		return cartInfoDTOList;
	}

	public void setCartInfoDTOList(List<CartInfoDTO> cartInfoDTOList) {
		this.cartInfoDTOList = cartInfoDTOList;
	}

	public int getTotalPrice() {
		return totalPrice;
	}

	public void setTotalPrice(int totalPrice) {
		this.totalPrice = totalPrice;
	}

	public Map<String, Object> getSession() {
		return session;
	}

	@Override
	public void setSession(Map<String, Object> session) {
		this.session = session;
	}
}
