package fastcampus.aop.part2.chapter08

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.webkit.URLUtil
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.ContentLoadingProgressBar
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

/** 웹뷰를 활용한 브라우저 간단한 브라우저 앱이다.
 * 구현 기능은 크게 , 홈버튼 , 이전버튼 , 다음버튼 , 페이지 새로고침 ,페이지 로딩시 로딩 진행 현황 프로그래스로 표시가있다
 * 나중에 시간이 된다면 설정 버튼과 즐겨찾기 기능도 구현해보고싶다 (room이나 sqlite를 활용)
 * **/

class MainActivity : AppCompatActivity() {


    // 초기 xml 뷰들의 바인딩
    // 늦은 초기화(by layz)를 이용해서 실제 사용 할때 뷰 초기화

    // 홈버튼
    private val goHomeButton: ImageButton by lazy {
        findViewById(R.id.goHomeButton)
    }

    // 주소창 에딧 텍스트
    private val addressBar: EditText by lazy {
        findViewById(R.id.addressBar)
    }

    // 이전 페이지 이동 버튼
    private val goBackButton: ImageButton by lazy {
        findViewById(R.id.goBackButton)
    }

    // 다음 페이지 이동 버튼
    private val goForwardButton: ImageButton by lazy {
        findViewById(R.id.goForwardButton)
    }

    // 위에서 아래로 스크롤시 새로고침 버튼
    private val refreshLayout: SwipeRefreshLayout by lazy {
        findViewById(R.id.refreshLayout)
    }

    // 웹뷰
    private val webView: WebView by lazy {
        findViewById(R.id.webView)
    }

    // 페이지 로딩중을 표시할 프로그래스바
    private val progressBar: ContentLoadingProgressBar by lazy {
        findViewById(R.id.progressBar)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 웹뷰 초기화
        initViews()
        bindViews()
    }

    override fun onBackPressed() {
        // 웹뷰에서 뒤로 갈수있으면 웹뷰 뒤로가기 없으면 앱종료
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    // 웹뷰 기본셋팅
    @SuppressLint("SetJavaScriptEnabled")
    private fun initViews() {

        //apply 스코프 함수를 이용해서 웹뷰 초기화
        //코틀린 api 고차함수 이용 (webview 객체를 넘겨서 webview 객체를 수정하고 반환함)

        webView.apply {
            webViewClient = WebViewClient(
            )
            // 현재 웹뷰에 사이트를 띄우기 위해서
            webChromeClient = WebChromeClient()
            // 사이트에서 제공하는 버튼 동작 시키기하기위ㅐ서
            settings.javaScriptEnabled = true
            loadUrl(DEFAULT_URL)
        }
    }

    // 각뷰들의 리스너 설정
    private fun bindViews() {
        goHomeButton.setOnClickListener {
            // 수정하기 편하게 DEFAULT_URL 별도의 상수로 빼서 작성
            webView.loadUrl(DEFAULT_URL)
        }

        // 주소창 에딧 텍스트에  값 입력한 경우
        addressBar.setOnEditorActionListener { v, actionId, event ->
            // IME_ACTION_DONE 액션 일때 (소프트 키보드에서 입력)
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val loadingUrl = v.text.toString()
                // 네트워크 url이 http 형식이 아닐경우 주소에 강제로 붙여줌
                if (URLUtil.isNetworkUrl(loadingUrl)) {
                    webView.loadUrl(loadingUrl)
                } else {
                    webView.loadUrl("http://$loadingUrl")
                }
            }

            return@setOnEditorActionListener false
        }

        goBackButton.setOnClickListener {
            webView.goBack()
        }

        goForwardButton.setOnClickListener {
            webView.goForward()
        }

        // 위에서 아래로 스크롤 시 웹뷰 새로 고침
        refreshLayout.setOnRefreshListener {
            webView.reload()
        }
    }


    // WebViewClient는 웹페이지를 로딩할때 생기는 콜백함수들로 구성

    // shouldOverridingUrlLoading : 웹뷰에서 url이 로딩될때 호출되어 앱에게 제어할 기회를 준다.

    // 페이지 새로 로딩될 때 처리 (컨텐츠 로딩)
    inner class WebViewClient : android.webkit.WebViewClient() {


        // 페이지가 로딩되는 첫 시점에 한번 호출된다
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)

            progressBar.show()
        }

        // 페이지 로딩이 끝나면 호출된다
        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)

            // 페이지가 로딩이 완료 되면 리프레시 버튼 사라짐
            refreshLayout.isRefreshing = false
            // 프로그래스바 숨김
            progressBar.hide()

            // 뒤로가기 앞으로가기 활성화 (canGoBack, canGoForward 기본 제공하는 메서드)
            goBackButton.isEnabled = webView.canGoBack()
            goForwardButton.isEnabled = webView.canGoForward()

            // 최종적으로 로딩된 url 표시
            addressBar.setText(url)
        }


    }


//    WebChromeClient는 웹페이지에서 일어나는 액션들에 관한 콜백함수들로 구성

//    onCreateWindow : 웹에서 새 창을 열때 호출된다.
//    onCloseWindow : 웹뷰가 창을 닫을 때 호출된다.

    // 프로그래스바에 현재 진행률 표시
    inner class WebChromeClient : android.webkit.WebChromeClient() {

        // 현재 로딩되는 페이지의 상태를 알려준다
        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            super.onProgressChanged(view, newProgress)

            progressBar.progress = newProgress
        }
    }

    // companion object를 사용해서 별도의 상수값 선언
    companion object {
        private const val DEFAULT_URL = "http://www.google.com"
    }
}
