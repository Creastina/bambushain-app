package app.bambushain.notification.calendar;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import app.bambushain.R;
import app.bambushain.notification.calendar.database.Event;
import dagger.hilt.android.qualifiers.ApplicationContext;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lombok.val;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class Notifier {
    private final static String TAG = Notifier.class.getName();
    public final int notificationId = Math.abs("Event".hashCode());
    @Inject
    @ApplicationContext
    Context context;

    @Inject
    public Notifier() {
    }

    public void updateNotification(String message) {
        Log.d(TAG, "updateNotification: Update notification with message " + message);
        val manager = context.getSystemService(NotificationManager.class);
        manager.notify(notificationId, createNotification(message));
    }

    public Notification createNotification(String message) {
        Log.d(TAG, "createNotification: Creating a persistent notification");

        return new NotificationCompat
                .Builder(context, context.getString(R.string.service_notification_channel_id))
                .setContentTitle(context.getString(R.string.service_title))
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setChannelId(context.getString(R.string.service_notification_channel_id))
                .setOngoing(true)
                .setSilent(true)
                .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
                .build();
    }

    public void showNotificationForToday(List<Event> events) {
        Schedulers.newThread().scheduleDirect(() -> {
            var message = context.getString(R.string.service_no_events);
            if (!events.isEmpty()) {
                if (events.size() == 1) {
                    message = context.getString(R.string.service_single_event, events.get(0).title);
                } else {
                    val lastEvent = events.get(events.size() - 1);
                    val otherEvents = events
                            .subList(0, events.size() - 1)
                            .stream()
                            .map(event -> event.title)
                            .collect(Collectors.joining(", "));

                    message = context.getString(R.string.service_multiple_events, otherEvents, lastEvent.title);
                }
            }

            updateNotification(message);
        });
    }
}
