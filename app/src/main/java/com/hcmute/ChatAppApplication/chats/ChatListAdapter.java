package com.hcmute.ChatAppApplication.chats;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.hcmute.ChatAppApplication.R;
import com.hcmute.ChatAppApplication.common.Constants;
import com.hcmute.ChatAppApplication.common.Util;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatListViewHolder> {

    // declare contex contain fragment
    private Context mContext;
    //declare list of chat
    private ArrayList<ChatListModel> chatListModelArrayList;

    //initiate chat list adapter
    public ChatListAdapter(Context mContext, ArrayList<ChatListModel> chatListModelArrayList) {
        this.mContext = mContext;
        this.chatListModelArrayList = chatListModelArrayList;
    }

    @NonNull
    @Override
    // action on creating
    public ChatListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.chat_layout, parent, false);
        return new ChatListViewHolder(view);
    }

    @Override
    //action on bind position to view
    public void onBindViewHolder(@NonNull ChatListViewHolder holder, int position) {

        ChatListModel chatListModel = chatListModelArrayList.get(position);

        holder.getFullName().setText(chatListModel.getUserName());
        holder.getLastMessage().setText(chatListModel.getLastMessage());

//        if(chatListModel.getPhotoFileName()!= null){
//
//            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(Constants.IMAGE_FOLDER+"/"+chatListModel.getPhotoFileName());
//            storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//                @Override
//                public void onSuccess(Uri uri) {
//                    Glide.with(mContext)
//                            .load(uri)
//                            .placeholder(R.drawable.profile_image_default)
//                            .error(R.drawable.profile_image_default)
//                            .into(holder.getProfilePic());
//                }
//            });
//        }

        if(!chatListModel.getUnreadMessageCount().equals("0")){
            holder.getUnreadCount().setVisibility(View.VISIBLE);
            holder.getUnreadCount().setText(chatListModel.getUnreadMessageCount());
        }else{
            holder.getUnreadCount().setVisibility(View.INVISIBLE);
        }

        if(!chatListModel.getLastMessage().equals("")){
            //holder.getLastMessage().setVisibility(View.VISIBLE);
            holder.getLastMessage().setText(chatListModel.getLastMessage());
        }else{
            holder.getLastMessage().setVisibility(View.GONE);
           // holder.getUnreadCount().setText("0");
        }

        if(!chatListModel.getLastSeenTime().equals("")){

            holder.getLastSeenTime().setText(Util.getTimeAgo(Long.parseLong(chatListModel.getLastSeenTime())));
            //holder.getLastSeenTime().setText(chatListModel.getLastSeenTime());
            Log.d("timestamp",Util.getTimeAgo(Long.parseLong(chatListModel.getLastSeenTime())));
        }else{

            holder.getUnreadCount().setText(R.string.now);
        }

        if(chatListModel.getPhotoFileName()!= null){
            Glide.with(mContext)
                    .load(chatListModel.getPhotoFileName())
                    .placeholder(R.drawable.profile_image_default)
                    .error(R.drawable.profile_image_default)
                    .into(holder.getProfilePic());
        }

        holder.getChatLilnearLayout().setOnClickListener(new View.OnClickListener() {
            @Override
            //action when click chat list item
            public void onClick(View view) {
                Intent intent = new Intent(mContext, MessagingChatActivity.class);
                intent.putExtra(Constants.USERID_INTENT_KEY, chatListModel.getUserId());
                intent.putExtra(Constants.USER_NAME_INTENT_KEY,chatListModel.getUserName());
                intent.putExtra(Constants.USER_PHOTO_INTENT_KEY,chatListModel.getPhotoFileName());
                mContext.startActivity(intent);
            }
        });

    }
    //func get chat list count
    @Override
    public int getItemCount() {
        return chatListModelArrayList.size();
    }
    //declare class for chat list view
    class ChatListViewHolder extends RecyclerView.ViewHolder{
        //declare text view for fullname, message, time, state for chat list item
        private TextView fullName,lastMessage,lastSeenTime,unreadCount;
        //decare custom image vieww for profile picture
        private CircleImageView profilePic;
        //declare linear for chat list item
        private LinearLayout chatLilnearLayout;
        //initiate chat list view
        public ChatListViewHolder(@NonNull View itemView) {
            super(itemView);

            fullName = itemView.findViewById(R.id.personFullNameTv);
            lastMessage = itemView.findViewById(R.id.lastMeessageTv);
            lastSeenTime = itemView.findViewById(R.id.lastSeenTv);
            unreadCount = itemView.findViewById(R.id.unreadCountTv);
            profilePic = itemView.findViewById(R.id.chatImagePerson);
            chatLilnearLayout = itemView.findViewById(R.id.chatLinearLayout);
        }
        //func to get full name of user
        public TextView getFullName() {
            return fullName;
        }
        //func to get last message between users
        public TextView getLastMessage() {
            return lastMessage;
        }
        //func to get time that friend read message
        public TextView getLastSeenTime() {
            return lastSeenTime;
        }
        //func to get number of message not read
        public TextView getUnreadCount() {
            return unreadCount;
        }
        //func to get profile picture of user
        public CircleImageView getProfilePic() {
            return profilePic;
        }
        //func to get linear layout for chat
        public LinearLayout getChatLilnearLayout() {
            return chatLilnearLayout;
        }
    }
}
