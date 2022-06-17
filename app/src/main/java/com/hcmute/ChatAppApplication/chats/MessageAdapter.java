package com.hcmute.ChatAppApplication.chats;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.appcompat.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.hcmute.ChatAppApplication.R;
import com.hcmute.ChatAppApplication.common.Constants;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewholder> {

    private Context context;
    private ArrayList<MessageModel> messageModelArrayList;
    private FirebaseAuth mAuth;
    private ActionMode actionMode;
    private ConstraintLayout selectedView;

    public MessageAdapter(Context context, ArrayList<MessageModel> messageModelArrayList) {
        this.context = context;
        this.messageModelArrayList = messageModelArrayList;
    }

    @NonNull
    @Override
    public MessageViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.message_layout,parent,false);
        return new MessageViewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewholder holder, int position) {

        MessageModel messageModel = messageModelArrayList.get(position);
        mAuth = FirebaseAuth.getInstance();
        String currentUserId = mAuth.getCurrentUser().getUid();
        String fromUserId = messageModel.getFrom();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        String dateTime = simpleDateFormat.format(new Date(messageModel.getTime()));
        String[] splitTimes = dateTime.split(" ");
        String messageTime = splitTimes[1];

        if(fromUserId.equals(currentUserId)){   //if current user: it will show send messages

            if(messageModel.getMessage_type().equals(Constants.MESSAGE_TYPE_TEXT)){

                holder.getSendMsgLL().setVisibility(View.VISIBLE);
                holder.getSendMessageImageLL().setVisibility(View.GONE);

            }else{

                holder.getSendMsgLL().setVisibility(View.GONE);
                holder.getSendMessageImageLL().setVisibility(View.VISIBLE);

            }

            holder.getReceivedMsgLL().setVisibility(View.GONE);

            holder.getTextViewMsgSent().setText(messageModel.getMessage());
            holder.getTextViewMsgSentTime().setText(messageTime);
            holder.getTextViewImagSentTime().setText(messageTime);
            Glide.with(context)
                    .load(messageModel.getMessage())
                    .placeholder(R.drawable.ic__622830_gallery_landskape_mountains_nature_photo_icon)
                    .into(holder.getSentImageIv());

        }
        else {          //else will show received message

            if(messageModel.getMessage_type().equals(Constants.MESSAGE_TYPE_TEXT)){

                holder.getReceivedMsgLL().setVisibility(View.VISIBLE);
                holder.getReceivedMessageImageLL().setVisibility(View.GONE);
            }
            else{

                holder.getReceivedMsgLL().setVisibility(View.GONE);
                holder.getReceivedMessageImageLL().setVisibility(View.VISIBLE);

            }

            holder.getSendMsgLL().setVisibility(View.GONE);

            holder.getTextViewMsgReceived().setText(messageModel.getMessage());
            holder.getTextViewImageReceivedTime().setText(messageTime);
            holder.getTextViewMsgReceivedTime().setText(messageTime);

            Glide.with(context)
                    .load(messageModel.getMessage())
                    .placeholder(R.drawable.ic__622830_gallery_landskape_mountains_nature_photo_icon)
                    .into(holder.getReceivedImageIv());
        }

        holder.getConstraintLayout().setTag(R.id.TAG_MESSAGE, messageModel.getMessage());
        holder.getConstraintLayout().setTag(R.id.TAG_MESSAGE_ID, messageModel.getMessage_id());
        holder.getConstraintLayout().setTag(R.id.TAG_MESSAGE_TYPE, messageModel.getMessage_type());

        holder.getConstraintLayout().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String messageType = view.getTag(R.id.TAG_MESSAGE_TYPE).toString();
                Uri uri = Uri.parse( view.getTag(R.id.TAG_MESSAGE).toString());

                if(messageType.equals(Constants.MESSAGE_TYPE_VIDEO)){

                    Intent intent = new Intent(Intent.ACTION_VIEW,uri);
                    intent.setDataAndType(uri,"video/mp4");
                    context.startActivity(intent);
                }
                else if(messageType.equals(Constants.MESSAGE_TYPE_IMAGE)){

                    Intent intent = new Intent(Intent.ACTION_VIEW,uri);
                    intent.setDataAndType(uri,"image/jpg");
                    context.startActivity(intent);
                }
            }
        });

        holder.getConstraintLayout().setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(actionMode!= null){
                    return false;
                }
                selectedView = holder.getConstraintLayout();
                selectedView.setBackgroundColor(context.getColor(R.color.teal_200));
                actionMode = ((AppCompatActivity)context).startSupportActionMode(callback);

                return true;

            }
        });

    }

    @Override
    public int getItemCount() {
        return messageModelArrayList.size();
    }

    class MessageViewholder extends RecyclerView.ViewHolder{

        private TextView textViewMsgSent,textViewMsgReceived,textViewMsgSentTime,textViewMsgReceivedTime,textViewImagSentTime,textViewImageReceivedTime;
        private LinearLayout sendMsgLL, receivedMsgLL,sendMessageImageLL,receivedMessageImageLL;
        private ImageView sentImageIv,receivedImageIv;
        private ConstraintLayout constraintLayout;

        public MessageViewholder(@NonNull View itemView) {
            super(itemView);

            textViewMsgSent = itemView.findViewById(R.id.textViewMessageSent);
            textViewMsgReceived = itemView.findViewById(R.id.textViewMessageReceived);
            textViewMsgSentTime = itemView.findViewById(R.id.textViewMessageSentTime);
            textViewMsgReceivedTime = itemView.findViewById(R.id.textViewMessageReceivedTime);
            textViewImagSentTime = itemView.findViewById(R.id.textViewImageSentTime);
            textViewImageReceivedTime = itemView.findViewById(R.id.textViewImageReceivedTime);

            sentImageIv = itemView.findViewById(R.id.ivImage__Sent);
            receivedImageIv = itemView.findViewById(R.id.ivImage__Received);

            sendMsgLL = itemView.findViewById(R.id.sentMessageLL);
            receivedMsgLL = itemView.findViewById(R.id.receeivedMessageLL);
            sendMessageImageLL = itemView.findViewById(R.id.sentMessageImageLL);
            receivedMessageImageLL = itemView.findViewById(R.id.receivedMessageImageLL);
            constraintLayout = itemView.findViewById(R.id.sentMessageCNl);
        }

        public TextView getTextViewMsgSent() {
            return textViewMsgSent;
        }

        public TextView getTextViewMsgReceived() {
            return textViewMsgReceived;
        }

        public TextView getTextViewMsgSentTime() {
            return textViewMsgSentTime;
        }

        public TextView getTextViewMsgReceivedTime() {
            return textViewMsgReceivedTime;
        }

        public LinearLayout getSendMsgLL() {
            return sendMsgLL;
        }

        public LinearLayout getReceivedMsgLL() {
            return receivedMsgLL;
        }

        public ConstraintLayout getConstraintLayout() {
            return constraintLayout;
        }

        public TextView getTextViewImagSentTime() {
            return textViewImagSentTime;
        }

        public TextView getTextViewImageReceivedTime() {
            return textViewImageReceivedTime;
        }

        public LinearLayout getSendMessageImageLL() {
            return sendMessageImageLL;
        }

        public LinearLayout getReceivedMessageImageLL() {
            return receivedMessageImageLL;
        }

        public ImageView getSentImageIv() {
            return sentImageIv;
        }

        public ImageView getReceivedImageIv() {
            return receivedImageIv;
        }
    }

    public ActionMode.Callback callback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {

            MenuInflater menuInflater = actionMode.getMenuInflater();
            menuInflater.inflate(R.menu.chat_on_hold_menu, menu);

            String selectedMessageType = String.valueOf(selectedView.getTag(R.id.TAG_MESSAGE_TYPE));
            if(selectedMessageType.equals(Constants.MESSAGE_TYPE_TEXT)){

                MenuItem item = menu.findItem(R.id.menuDownload);
                item.setVisible(false);
            }
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {

            String selectedMessageId = String.valueOf(selectedView.getTag(R.id.TAG_MESSAGE_ID));
            String selectedMessage = String.valueOf(selectedView.getTag(R.id.TAG_MESSAGE));
            String selectedMessageType = String.valueOf(selectedView.getTag(R.id.TAG_MESSAGE_TYPE));

            switch (menuItem.getItemId()){

                case R.id.menuFrwrd:
                    actionMode.finish();
                    break;
                case R.id.menuShare:
                    if(selectedMessageType.equals(Constants.MESSAGE_TYPE_TEXT)){

                        Intent intentShared = new Intent();
                        intentShared.setAction(Intent.ACTION_SEND);
                        intentShared.putExtra(Intent.EXTRA_TEXT, selectedMessage);
                        intentShared.setType("text/plain");
                        context.startActivity(intentShared);
                    }
                    else {
                        if(context instanceof MessagingChatActivity){
                            ((MessagingChatActivity)context).downloadFile(selectedMessageId, selectedMessageType, true);
                        }

                    }

                    actionMode.finish();
                    break;
                case R.id.menuDelete:
                    //Toast.makeText(context, "Done", Toast.LENGTH_SHORT).show();
                    if(context instanceof MessagingChatActivity){
                        //Toast.makeText(context, "Done", Toast.LENGTH_SHORT).show();
                        ((MessagingChatActivity)context).deleteMessage(selectedMessageId, selectedMessageType);
                    }
                    actionMode.finish();
                    break;
                case R.id.menuDownload:
                    if(context instanceof MessagingChatActivity){
                        ((MessagingChatActivity)context).downloadFile(selectedMessageId, selectedMessageType, false);
                    }
                    actionMode.finish();
                    break;


            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {

            actionMode = null;
            selectedView.setBackgroundColor(context.getColor(R.color.white));
        }
    };
}
