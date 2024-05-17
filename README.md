# POST_OpenSourcePrj
해당 프로젝트는 라즈베리 파이의 미세먼지 센서를 활용하여 충북대학교 캠퍼스 내의 미세먼지 데이터를 수집, 측정 및 예측하는 안드로이드 앱을 개발하는 것입니다.

## 목차
1. [주요 기능](#주요-기능)
2. [설치](#설치)
3. [기여자](#기여자)
4. [버전 정보](#버전-정보)

## 주요 기능
- Advertisement Scan
라즈베리 파이에서 Advertise하는 sensing data를 스캔하여 csv파일에 저장
- Bluetooth Pairing
라즈베리 파이와 페어링 연결
- Send Data
csv파일에 저장된 데이터를 서버에 전송

## 설치
1. Adnroid Studio 설치
2. 프로젝트 clone
```bash
git clone https://github.com/marmot8080/POST_OpenSourcePrj.git
```
3. Main Menu > Build > Build Bundle(s) / APK(s)
4. 안드로이드 폰에서 다운로드
* 안드로이드 폰에서 애플리케이션 > 특별한 접근 > 모든 파일에 대한 접근 > OpenSourcePrj 허용 해줘야 사용 가능

## 기여자
| 기여자         | 연락처                  |
|-------------|----------------------|
| marmot8080  | marmot8080@gmail.com |
| Taeho24 | teahotiger@gmail.com           |
| Bakhyoungwoo | @gmail.com           |

## 버전 정보
| 버전   | 주요 기능 및 변경 사항                   |
|------|---------------------------------|
| v0.4 | 라즈베리 파이와의 블루투스 페어링 기능 추가        |                       
| v0.3 | 서버 크롤링 기능 추가                    |
| v0.2 | 라즈베리 파이 Advertiesement 스캔 기능 추가 |
| v0.1 | 서버에 데이터 전송 기능 추가                |