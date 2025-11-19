package com.uyscuti.social.call.di

const val RESULT_KEY = "RESULT_KEY"



//@Module
//@InstallIn(SingletonComponent::class)
//object NotificationModule {
//
//
//    @Provides
//    fun provideNotificationBuilder(
//        @ApplicationContext context: Context
//    ): NotificationCompat.Builder {
//        val flag =
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//                PendingIntent.FLAG_MUTABLE
//            } else
//                0
//        val customView = RemoteViews(context.packageName, R.layout.custom_call_notification)
//        val answerIntent = Intent(context,  AnswerCallReceiver::class.java)
//        answerIntent.putExtra("message", "message from chat")
//        val answerPendingIntent = PendingIntent.getBroadcast(
//            context, 0, answerIntent, PendingIntent.FLAG_UPDATE_CURRENT
//        )
//
//        customView.setOnClickPendingIntent(R.id.answer_button, answerPendingIntent)
//        val declineIntent = Intent(context, DeclineCallReceiver::class.java)
//        answerIntent.putExtra("message", "message from chat")
//        val declinePendingIntent = PendingIntent.getBroadcast(
//            context, 0, declineIntent, PendingIntent.FLAG_UPDATE_CURRENT
//        )
//        customView.setOnClickPendingIntent(R.id.decline_button, declinePendingIntent)
//
//        return NotificationCompat.Builder(context, "Main Channel ID")
//            .setSmallIcon(R.drawable.ic_baseline_notifications_24)
//            .setPriority(NotificationCompat.PRIORITY_HIGH)
//            .setAutoCancel(true)
//            .setContentTitle("Flash call notification")
////            .setStyle(notificationStyle)
////            .addAction(replyAction)
//            .setCustomContentView(customView)
////            .setContentIntent(pendingIntent)
////            .setAutoCancel(true)
//
//    }
//
//    @Provides
//    fun provideNotificationManager(
//        @ApplicationContext context: Context
//    ): NotificationManagerCompat {
//        val notificationManager = NotificationManagerCompat.from(context)
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channel = NotificationChannel(
//                "Main Channel ID",
//                "Main Channel",
//                NotificationManager.IMPORTANCE_HIGH
//            ).apply {
//                lightColor = Color.GREEN
//                enableLights(true)
//                vibrationPattern = longArrayOf(100,200,300,400,500,4)
//            }
////            val manager = getSystemService(context.NOTIFICATION_SERVICE) as NotificationManager
//            notificationManager.createNotificationChannel(channel)
//        }
//        return notificationManager
//    }
//
//    @Provides
//    fun provideNotificationManage(
//        @ApplicationContext context: Context
//    ): NotificationManager {
//        val notificationManager = NotificationManagerCompat.from(context) as NotificationManager
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channel = NotificationChannel(
//                "Video Call Channel ID",
//                "Video Call Channel",
//                NotificationManager.IMPORTANCE_HIGH
//            ).apply {
//                lightColor = Color.GREEN
//                enableLights(true)
//                vibrationPattern = longArrayOf(100,200,300,400,500,4)
//            }
//            notificationManager.createNotificationChannel(channel)
//        }
//        return notificationManager
//    }
//
////    private fun createNotificationChannel(){
////        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
////            val channel = NotificationChannel("Video_Call_Id","Video_call_Notifications", NotificationManager.IMPORTANCE_HIGH).apply {
////                lightColor = Color.GREEN
////                enableLights(true)
////                vibrationPattern = longArrayOf(100,200,300,400,500,4)
////            }
////
////            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
////            manager.createNotificationChannel(channel)
////        }
////    }
//    fun extractContentTitle(notificationBuilder: NotificationCompat.Builder): CharSequence? {
//        return notificationBuilder.build().extras?.getCharSequence(NotificationCompat.EXTRA_TITLE)
//    }
//
//}