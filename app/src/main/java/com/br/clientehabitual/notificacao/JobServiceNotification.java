package com.br.clientehabitual.notificacao;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.br.clientehabitual.MainActivity;
import com.br.clientehabitual.R;
import com.br.clientehabitual.banco.daos.ClienteDAO;
import com.br.clientehabitual.banco.daos.InadimplenciaDAO;
import com.br.clientehabitual.models.Cliente;
import com.br.clientehabitual.models.Inadimplencia;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Pattern;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class JobServiceNotification  extends JobService {
    @Override
    public boolean onStartJob(JobParameters params) {
        new AsyncTaskNotification(this).execute(params);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }
    public void gerarNotificacao(Context context, Inadimplencia inadimplencia){
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,0,new Intent(context, MainActivity.class),0);
        String NOTIFICATION_CHANNEL_ID = "cliente_habitual_channel_id_01";


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "Notificações de atrazados", NotificationManager.IMPORTANCE_DEFAULT);

            // Configure the notification channel.
            notificationChannel.setDescription("Notificação de pagamentos atrazados");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);
        DecimalFormat df = new DecimalFormat("#0.00");
        notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setTicker("Cliente Habitual! 'Pagamentos atrazados!'")
                .setPriority(Notification.PRIORITY_MAX)
                .setContentTitle(inadimplencia.getCliente().getNome())
                .setContentIntent(pendingIntent)
                .setContentInfo("R$ "+ df.format(inadimplencia.getTotal()).replaceAll(Pattern.quote("."),","))
                .setContentText("Data para pagamento expirou!");

        notificationManager.notify((int)inadimplencia.getCliente().getId(), notificationBuilder.build());
    }

    private class AsyncTaskNotification extends AsyncTask<JobParameters, Void, String> {
        private JobServiceNotification jobServiceNotification;
        public AsyncTaskNotification(JobServiceNotification j){
            jobServiceNotification = j;
        }
        @Override
        protected String doInBackground(JobParameters... params) {
            ClienteDAO clienteDAO = new ClienteDAO(getApplicationContext());
            ArrayList<Cliente> clientes = clienteDAO.listaClientes();
            InadimplenciaDAO inadimplenciaDAO = new InadimplenciaDAO(getApplicationContext());
            for (Cliente c : clientes){
                Inadimplencia inadimplencia = inadimplenciaDAO.getInadimpleciaCliente(c);
                if (inadimplencia != null){
                    if(inadimplencia.getDataFim() != null && inadimplencia.isQuitada() == false){
                        Calendar calendar = Calendar.getInstance();
                        if (calendar.getTime().after(inadimplencia.getDataFim().getTime())){
                            gerarNotificacao(getApplicationContext(), inadimplencia);
                        }
                    }
                }
            }
            jobServiceNotification.jobFinished(params[0], true);
            return null;
        }
    }
}
