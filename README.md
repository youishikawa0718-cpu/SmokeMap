# SmokeMap - 全国喫煙所マップ

近くの喫煙所をすばやく見つけられるAndroidアプリ。現在地から半径指定で周辺の喫煙所を検索し、マップまたはリストで確認できます。

## プロジェクト概要

### 主な機能

- **マップ表示** — Google Mapsで周辺の喫煙所をマーカー表示（自動クラスタリング対応）
- **現在地検索** — GPS位置情報から半径（500m / 1,000m / 2,000m）で喫煙所を検索
- **カテゴリフィルタ** — 屋内 / 屋外 / 飲食店併設で絞り込み
- **スポット名検索** — 検索バーからスポット名で絞り込み
- **ソート** — 距離順・評価順の切り替え
- **リスト表示** — マップとリストの切り替え（Pull-to-refresh対応）
- **スポット詳細** — 場所の詳細情報、Google Mapsナビ連携
- **レビュー** — 星評価とコメントの投稿・削除
- **スポット報告** — 閉鎖・場所誤り等の報告
- **お気に入り** — ローカルに保存してすぐアクセス
- **オフライン対応** — ネットワーク未接続時はキャッシュデータを表示

### アーキテクチャ

```
[MapScreen / DetailScreen]  ← Jetpack Compose UI
        │
   [ViewModel]              ← UI状態管理、クライアント側フィルタ/ソート
        │
  [SpotRepository]          ← データ取得の一元化、オフラインフォールバック
     ┌──┴──┐
[Supabase API]  [Room DB]   ← リモート(REST) / ローカルキャッシュ
```

- バックエンドは Supabase RPC `nearby_spots` で半径検索（PostGIS距離計算）
- 全件取得はせず、ユーザーの現在地＋指定半径内のスポットのみ取得
- マーカーは Google Maps Compose の `Clustering` で自動クラスタリング

## 使用している主な技術

| カテゴリ | 技術 | バージョン |
|---|---|---|
| 言語 | Kotlin | 2.1.0 |
| UI | Jetpack Compose + Material 3 | BOM 2025.01.01 |
| 地図 | Google Maps Compose + Clustering | 6.4.1 |
| 位置情報 | Play Services Location | 21.3.0 |
| HTTP | Retrofit + OkHttp | 2.11.0 |
| シリアライズ | Kotlinx Serialization | 1.7.3 |
| ローカルDB | Room (KSP) | 2.7.1 |
| 画像読み込み | Coil Compose | 2.7.0 |
| バックエンド | Supabase (PostgreSQL + REST API) | — |
| ビルド | AGP 9.1.0, secrets-gradle-plugin 2.0.1 | — |
| テスト | JUnit 4 + Mockito + Coroutines Test | — |

## 必要な環境変数

`local.properties` に以下を設定します（Git管理外）。

| 変数名 | 説明 | 取得元 |
|---|---|---|
| `MAPS_API_KEY` | Google Maps API キー | [Google Cloud Console](https://console.cloud.google.com/) で Maps SDK for Android を有効化 |
| `SUPABASE_URL` | Supabase プロジェクトURL | Supabase ダッシュボード > Settings > API |
| `SUPABASE_ANON_KEY` | Supabase anon (public) キー | 同上 |

```properties
# local.properties
MAPS_API_KEY=your_google_maps_api_key
SUPABASE_URL=https://your-project.supabase.co
SUPABASE_ANON_KEY=your_supabase_anon_key
```

> `local.defaults.properties` にはデフォルト値（`MAPS_API_KEY=DEFAULT_API_KEY`）が入っています。実際の値は必ず `local.properties` で上書きしてください。

## コマンド一覧

```bash
# ビルド
./gradlew assembleDebug

# テスト（ユニットテスト）
./gradlew testDebugUnitTest

# Lint チェック
./gradlew lintDebug

# APK インストール（実機/エミュレータ接続時）
./gradlew installDebug

# クリーンビルド
./gradlew clean assembleDebug
```

## ディレクトリ構成

```
SmokeMap3/
├── app/
│   ├── build.gradle.kts              # アプリモジュールのビルド設定
│   └── src/main/
│       ├── AndroidManifest.xml        # パーミッション、Maps APIキー設定
│       └── java/com/example/smokemap/
│           ├── MainActivity.kt        # エントリポイント（Compose ホスト）
│           ├── MyApplication.kt       # Application クラス
│           ├── data/
│           │   ├── SpotRepository.kt          # データ取得の一元化、オフラインフォールバック
│           │   ├── local/
│           │   │   ├── SmokeMapDatabase.kt    # Room データベース定義
│           │   │   ├── DatabaseProvider.kt    # DB インスタンス提供
│           │   │   ├── SpotDao.kt             # スポットキャッシュ DAO
│           │   │   ├── SpotEntity.kt          # スポットキャッシュ エンティティ
│           │   │   ├── FavoriteSpotDao.kt     # お気に入り DAO
│           │   │   ├── FavoriteSpotEntity.kt  # お気に入り エンティティ
│           │   │   └── DeviceId.kt            # デバイスID生成
│           │   └── remote/
│           │       ├── SupabaseApi.kt         # Retrofit API定義 + DTO
│           │       └── SupabaseClient.kt      # Retrofit クライアント生成
│           ├── di/                            # DI（Hilt 導入準備中）
│           ├── domain/model/
│           │   ├── Spot.kt                    # スポット ドメインモデル
│           │   └── Review.kt                  # レビュー ドメインモデル
│           ├── navigation/
│           │   ├── Screen.kt                  # 画面ルート定義
│           │   └── SmokeMapNavGraph.kt        # Navigation Compose グラフ
│           └── ui/
│               ├── map/
│               │   ├── MapScreen.kt           # マップ/リスト画面
│               │   ├── MapViewModel.kt        # マップ画面の状態管理
│               │   └── SpotClusterItem.kt     # クラスタリング用アイテム
│               ├── detail/
│               │   ├── SpotDetailScreen.kt    # スポット詳細・レビュー画面
│               │   └── SpotDetailViewModel.kt # 詳細画面の状態管理
│               ├── favorites/
│               │   └── FavoritesScreen.kt     # お気に入り一覧画面
│               └── theme/                     # Material 3 テーマ定義
├── build.gradle.kts                   # ルートビルド設定
├── gradle/libs.versions.toml          # バージョンカタログ
├── local.properties                   # APIキー等（Git管理外）
├── local.defaults.properties          # デフォルト値
└── settings.gradle.kts                # プロジェクト設定
```

## 開発環境の構築方法

### 前提条件

- **Android Studio** Ladybug (2024.2) 以降
- **JDK** 11 以上（Android Studio 同梱のものでOK）
- **Android SDK** API 26 以上（minSdk = 26, targetSdk = 36）
- **Google Maps API キー**（Maps SDK for Android が有効であること）
- **Supabase プロジェクト**（`nearby_spots` RPC関数がデプロイ済みであること）

### セットアップ手順

1. **リポジトリをクローン**
   ```bash
   git clone https://github.com/<your-username>/SmokeMap3.git
   cd SmokeMap3
   ```

2. **`local.properties` にAPIキーを設定**
   ```properties
   MAPS_API_KEY=your_google_maps_api_key
   SUPABASE_URL=https://your-project.supabase.co
   SUPABASE_ANON_KEY=your_supabase_anon_key
   ```

3. **Android Studio でプロジェクトを開く**
   - File > Open から `SmokeMap3` フォルダを選択
   - Gradle Sync が自動で走り、依存関係がダウンロードされる

4. **エミュレータまたは実機で実行**
   - エミュレータの場合：Google Play イメージを使用すること（Maps SDK に必要）
   - 実機の場合：USB デバッグを有効にして接続

### Supabase バックエンド

アプリが利用するテーブルとRPC関数：

| リソース | 種別 | 説明 |
|---|---|---|
| `spots` | テーブル | 喫煙所データ（id, name, latitude, longitude, category, status 等） |
| `reviews` | テーブル | レビューデータ（spot_id, user_id, rating, comment 等） |
| `spot_reports` | テーブル | 報告データ（spot_id, user_id, reason, comment） |
| `nearby_spots` | RPC関数 | 緯度経度+半径で近傍スポットを検索（PostGIS距離計算、avg_rating付き） |

## トラブルシューティング

### ビルドエラー

| 症状 | 原因と対処 |
|---|---|
| `MAPS_API_KEY` が見つからない | `local.properties` に `MAPS_API_KEY=...` が設定されているか確認。`secrets-gradle-plugin` が `local.properties` を読み込む |
| `SUPABASE_URL` / `SUPABASE_ANON_KEY` が見つからない | 同上。`BuildConfig` にこれらの値が反映される |
| Gradle Sync 失敗 | Android Studio を最新版に更新。AGP 9.1.0 は Ladybug 以降が必要 |
| KSP エラー（Room関連） | `./gradlew clean` してから再ビルド。Room のスキーマ変更時はマイグレーションを確認 |

### 実行時エラー

| 症状 | 原因と対処 |
|---|---|
| 地図が表示されない（灰色のまま） | `MAPS_API_KEY` が無効、またはGoogle Cloud Console で Maps SDK for Android が有効になっていない |
| スポットが表示されない | Supabase の URL/キーが正しいか確認。Logcat で HTTP レスポンスを確認（OkHttp BODY レベルでログ出力される） |
| 位置情報が取得できない | エミュレータの場合、Extended Controls > Location で位置を手動設定。実機の場合、端末の位置情報がONか確認 |
| オフライン時にデータが表示されない | 一度もオンラインで取得していない場合、キャッシュが空のため表示されない。先にオンラインでスポットを取得する |
| 「Google Play services is updating」 | エミュレータの Google Play Services が古い。System Image を更新するか、実機で試す |

### デバッグのヒント

- **Logcat フィルタ**: `OkHttp` タグでAPI通信の詳細が確認できる（リクエスト/レスポンスのBODYレベル）
- **Room データ確認**: Android Studio の App Inspection > Database Inspector でローカルDBの中身を確認
- **位置情報のモック**: エミュレータの Extended Controls、または `adb emu geo fix <longitude> <latitude>` で任意の位置を送信

## ライセンス

MIT License
