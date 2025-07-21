package com.sgs.desiKahaniyaAdult;

import androidx.appcompat.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.AsyncTask;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;

public class DownloadAudioHelper {

    public static class DownloadFileFromURL extends AsyncTask<Void, Integer, Boolean> {
        private final Context context;
        private final String storyName;
        private final String fileURL;
        private final AlertDialog dialog;
        private final ProgressBar progressBar;
        private final TextView progressIndicator;
        private final TextView downloadSize;
        private int fileLength;

        public DownloadFileFromURL(Context context, String storyName, String fileURL,
                                   AlertDialog dialog, ProgressBar progressBar,
                                   TextView progressIndicator, TextView downloadSize) {
            this.context = context;
            this.storyName = storyName;
            this.fileURL = fileURL;
            this.dialog = dialog;
            this.progressBar = progressBar;
            this.progressIndicator = progressIndicator;
            this.downloadSize = downloadSize;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                URL url = new URL(fileURL);
                URLConnection connection = url.openConnection();
                connection.connect();
                fileLength = connection.getContentLength();

                InputStream input = new BufferedInputStream(url.openStream());
                ContextWrapper cw = new ContextWrapper(context);
                File directory = cw.getDir("Download", Context.MODE_PRIVATE);
                File file = new File(directory, storyName.replaceAll(" ", "_") + ".mp3");
                OutputStream output = new FileOutputStream(file);

                byte[] data = new byte[1024];
                int total = 0, count;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    publishProgress((int) ((total * 100L) / fileLength));
                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();
                return true;

            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            int progress = values[0];
            progressBar.setProgress(progress);
            progressIndicator.setText(progress + "%");
            int sizeInMB = (fileLength / 1024) / 1024;
            int downloadedMB = (progress * sizeInMB) / 100;
            downloadSize.setText("(" + downloadedMB + "MB/" + sizeInMB + "MB)");
            downloadSize.setVisibility(TextView.VISIBLE);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            dialog.dismiss();
            Toast.makeText(context, result ? "Download Completed" : "Download Failed", Toast.LENGTH_SHORT).show();
        }
    }
}
