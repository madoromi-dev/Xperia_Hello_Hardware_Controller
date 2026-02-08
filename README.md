# Xperia Hello! (G1209) Hardware Controller & Motion Player

### 言語 / Language : 日本語 | [English](README_EN.md)

> [!CAUTION]
> "Xperia Hello!" デバイスの Bootloader Unlock と root 化が必要です。

> [!NOTE]
> このプロジェクトは開発段階かつ趣味レベルの実装です。ご意見やご指摘などありましたら、遠慮なくご連絡ください！

![Main image](https://github.com/madoromi-dev/Xperia_Hello_Hardware_Controller/blob/main/image.jpg?raw=true)

## 概要

すでにサービスが終了し、完全に動作しなくなった Sony のコミュニケーションロボット "Xperia Hello!" (G1209 / コードネーム Bright) のハードウェア部分を Android アプリから操作可能にする、Java で記述されたコントローラーと動作のモーションプレイヤー、およびデモアプリです。

動作の様子は [YouTube Shorts](https://youtube.com/shorts/Syec_pLMPwQ) に公開しています。

### 説明

"Xperia Hello!" は 2023 年 3 月 31 日のサービス終了以来、基本的なコミュニケーションを含めたすべての機能が使えなくなっています。偶然フリマサイトで格安で取引されているのを見つけて、ビルドクオリティーの高さから購入してしまったことがこのプロジェクトのはじまりです。幸い、先駆者の方が通信プロトコルや Kotlin でのコントロール方法を公開してくださっていましたので、こちらを参考に、クラッシュの修正や動作の最適化、Java でのリライトを行ったコントローラーを作成しました。ついでに、アプリから頷きやウィンクなどのモーションを実行しやすいように、JSON を利用したモーションプレイヤーも追加しています。

### 仕組み

> [!IMPORTANT]
> 本プロジェクトは、Sony 公式の SDK、バイナリなどの知的財産を含んでおらず、またそれらの侵害を目的とするものではありません。本ソフトウェアは有志による解析と公開情報に基づいた非公式実装であり、Sony Corporation およびその関連会社とは一切関係ありません。

Xperia Hello! は、ハードウェア部分 (首左右上下・ボディーの左右の動き、目と首の LED) をシリアルポート (`/dev/ttyHS1`) で制御しており、何故か Bootloader Unlock も可能になっているため、スーパーユーザー権限を用いることで制御できるようになっています。

## 構成

### 機能

- **モーター操作**: 首の上下 (Tilt) 、左右 (Pan) 、本体の回転 (Body) をコントロールできます。可動域制限の実装によりハードウェアの保護にも配慮しています。
- **LED 操作**: 目と首元の LED をコントロールできます。目の LED はビットマスク変換により、直感的な数値 (開眼度合い) 指定で表情を作れます。首元の LED は HEX RGB 指定です。
- **モーション再生**: 上記の一連の動作を JSON ファイルで定義して再生できます。非同期処理によるスムーズなアニメーション再生を可能にします。目 LED とモーターのレイヤー、および首 LED のレイヤーで分離して管理されるため、並列実行が可能になっており、豊かな表現力を実現できると思います。

### ソースコード

- **`BrightController.java`**: 通信を担当するコントローラーです。
- **`BrightMotionPlayer.java`**: モーションプレイヤーのクラスです。
- **`MainActivity.java`**: 上記のコントローラーとモーションプレイヤーを試せるアクティビティです。
- **`assets/motions.json`**: モーションが保存されている JSON ファイルです。

## 導入とビルド

### 1. "Xperia Hello!" の準備

"Xperia Hello!" の Bootloader Unlock と root 化を済ませておいてください。
具体的な方法やパッチ適用済みの `boot.img` は [**Sony-Xperia-Hello-Control** (by r00li)](https://github.com/r00li/Sony-Xperia-Hello-Control) でも紹介されています。

### 2. 使用ライブラリの追加

新規プロジェクトを作成する場合は、シリアルポート通信を行う以下のライブラリをインポートしてください。
~~~
dependencies {
    implementation 'io.github.xmaihh:serialport:2.1.1'
}
~~~

### 3. ビルドしてインストールする

このプロジェクトを Clone してビルドするか、[ビルド済みの APK](https://github.com/madoromi-dev/Xperia_Hello_Hardware_Controller/blob/main/app-release.apk) をインストールしてデモアプリをお試しいただけます。スーパーユーザーのアクセス許可を求めるポップアップが表示されたら、許可をタップしてください。

## ライセンス

### 謝辞

シリアル通信プロトコルの解析およびハードウェア制御の基礎は、以下のプロジェクトを参考に作成しました。素晴らしい知見を共有してくださった作者に深く感謝いたします！

[**Sony-Xperia-Hello-Control** (by r00li)](https://github.com/r00li/Sony-Xperia-Hello-Control)
~~~
Copyright (c) 2024 Andrej Rolih

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
~~~

### 商標

- "Xperia" は Sony Corporation の商標または登録商標です。

### このプロジェクトのライセンス

本ソフトウェアは [MIT License](LICENSE.txt) の下で公開されています。


