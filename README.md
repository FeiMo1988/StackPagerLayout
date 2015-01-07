# StackPagerLayout
StackPagerLayout支持像翻书一样的边沿滑动,继承于AdapterView,实现了控件的复用功能
<img 
	src="http://img.blog.csdn.net/20150107155312250?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvSGVsbG9fX1plcm8=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/Center"
/>

StackPagerLayout.java:
     reloadAndSmoothMoveNext():重新load下一个view,并平滑过度到该view
     movePre:前移
     moveNext:后移
     smoothMoveToPosition:移动到具体位置
     

