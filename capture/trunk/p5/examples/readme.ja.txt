CaptureDSライブラリ 仮ドキュメント
　　　　　　　　　　　　　　　　　　　　　　　　　　　　　 2011.04.06 版
　　　　　　　　　　　　　　　　　　　　　　　　　 arc@dmz (c) 2010-2011
────────────────────────────────────


◆ライブラリの使い方:

1. DirectShow Java Wrapperの入手

 http://www.humatic.de/htools/dsj/download.htm から、
 最新版のdsj(11/04/06時点で0_8_62)をダウンロード、
 解凍して出てきたファイルのうち dsj.dll と dsj.jar を使います。

2. DirectShow Java WrapperとCaptureDSの利用

 dsj.dll, dsj.jar, CaptureDS.jar を、
 ライブラリの機能を使いたいスケッチが保存されたフォルダの中にある
 code フォルダへ移動(またはコピー)すれば、
 当該スケッチの冒頭に

	import processing.video.*;

 あるいは

	import processing.video.CaptureDS;

 と書いてCaptureDSの機能を使えるようになります。

3. とりあえずキャプチャできるか確認したい

 dsj.dll, dsj.jar, CaptureDS.jar を、
 GettingStartedCaptureDS フォルダの中にある code フォルダへ
 移動(またはコピー)して、
 GettingStartedCaptureDS.pde を開いてみてください。


◆クラスの詳しい使い方:

下記リファレンスを読んでみてください。
http://digitalmuseum.jp/software/nui/processing/CaptureDS/reference/


◆ライセンスなど:

CaptureDS.jar はJavaでWebカメラの映像入力を使えるようにする capture プロ
ジェクトの一環で開発されており、MPL 1.1/GPL 2.0/LGPL 2.1ライセンスの元で
配布されています。
詳しくは下記サイトにある capture プロジェクト本体の配布物をご覧ください。
http://mr.digitalmuseum.jp/

また、CaptureDS.jar の利用には DirectShow Java Wrapper が必要です。
従って、CaptureDS.jar の利用者は DirectShow Java Wrapper のライセンス
にも束縛される点に注意してください。


────────────────────────────────────
arc@dmz

mail:  arc@digitalmuseum.jp
web:   http://digitalmuseum.jp/