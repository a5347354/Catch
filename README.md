# Catch
<img src="ntubbroadcast-android_broadcast_layout/google play/icon.png" width="450"/>
<p>
說明：從Bitbucket轉移至Github，此專案為大學的專題。
<br>主要分為廠商以及使用者兩端，不需透過其他的 Wi-Fi 網路或著 3G 行動網路，廠商藉由藍牙即可傳送圖片、文字、影音等訊息至使用者端，並會立即顯示在 APP 上;而使用者端則是用 P2P 的方式傳遞訊息，形成私人聊天室。
<br>Catch!在將訊息傳出去或著接收訊息前，會事先進行搜尋、配對以及連線的順序，此三階段都成功即可將訊息傳送出去或接收到訊息，連線上會採取網狀的方式進行 P2P 連線，當傳送出訊息或接收到訊息會立刻存進手機本身內建的SQLite，並判斷時間戳記,超過 30 天則會將訊息刪除。此外廠商端發送訊息還可以設定每幾秒或每分鐘就廣播一次訊息。Ex:當燈會時人潮眾多，活動結束後會造成疏散人潮的問題，廠商可藉由 APP 疏散人潮的路線，顧客可由此 APP 依照廠商所給予的疏散路線做選擇。

It's a communication software through Bluetooth instead of Internet
It is moved from bitbucket.


<img src="ntubbroadcast-android_broadcast_layout/google play/Screenshot_2015-11-07-22-38-38.png" width="300"/>
<img src="ntubbroadcast-android_broadcast_layout/google play/Screenshot_2015-11-07-22-38-40.png" width="300"/>
<p>
<img src="ntubbroadcast-android_broadcast_layout/google play/Screenshot_2015-11-07-22-38-43.png" width="300"/>
<img src="ntubbroadcast-android_broadcast_layout/google play/Screenshot_2015-11-07-22-38-49.png" width="300"/>
