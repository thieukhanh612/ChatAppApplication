package com.hcmute.ChatAppApplication.findfrnd;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.hcmute.ChatAppApplication.R;
import com.hcmute.ChatAppApplication.common.Constants;
import com.hcmute.ChatAppApplication.common.NodeNames;
import com.hcmute.ChatAppApplication.common.Util;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendAdapter extends RecyclerView.Adapter<FindFriendAdapter.FindFriendViewHolder> {

    private ArrayList<FindFriendModel> frnds;
    private Context context;
    private FirebaseDatabase database;
    private FirebaseUser currentUser;
    private DatabaseReference databaseReference;
    private String userId;

    public FindFriendAdapter(ArrayList<FindFriendModel> frnds, Context context) {
        this.frnds = frnds;
        this.context = context;
    }

    @NonNull
    @Override
    public FindFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.find_friend_layout, parent, false);
        return new FindFriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FindFriendViewHolder holder, int position) {

        FindFriendModel findFriendModel = frnds.get(position);
        holder.getPersonName().setText(findFriendModel.getPersonName());

        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(Constants.IMAGE_FOLDER+"/"+findFriendModel.getPhotoFileName());
        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(context)
                        .load(uri)
                        .placeholder(R.drawable.profile_image_default)
                        .error(R.drawable.profile_image_default)
                        .into(holder.getImage());
            }
        });

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference().child(NodeNames.FRIEND_REQUESTS);


        if(findFriendModel.isRequestHasSent()){

            holder.getCancelRequestBtn().setVisibility(View.VISIBLE);
            holder.getSendFriendRequestBtn().setVisibility(View.GONE);
        }
        else {
            holder.getCancelRequestBtn().setVisibility(View.GONE);
            holder.getSendFriendRequestBtn().setVisibility(View.VISIBLE);
        }

        holder.getSendFriendRequestBtn().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                holder.getSendFriendRequestBtn().setVisibility(View.GONE);
                holder.getFrndReqPb().setVisibility(View.VISIBLE);

                userId = findFriendModel.getUserId();

                databaseReference.child(currentUser.getUid()).child(userId).child(NodeNames.REQUESTS_TYPE)
                        .setValue(Constants.FRIEND_REQUESTS_SENT).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){

                                databaseReference.child(userId).child(currentUser.getUid()).child(NodeNames.REQUESTS_TYPE)
                                        .setValue(Constants.FRIEND_REQUESTS_RECEIVED).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            Toast.makeText(context, context.getString(R.string.successfully_sent_request), Toast.LENGTH_SHORT).show();

                                            String tittle = "New Friend Request Arrived";
                                            String message = "Friend sent from"+ currentUser.getDisplayName();

                                            Util.sendNotification(context,message,tittle,userId);

                                            holder.getSendFriendRequestBtn().setVisibility(View.GONE);
                                            holder.getFrndReqPb().setVisibility(View.GONE);
                                            holder.getCancelRequestBtn().setVisibility(View.VISIBLE);

                                        }
                                        else{
                                            Toast.makeText(context, context.getString(R.string.unsuccessfully_sent_request), Toast.LENGTH_SHORT).show();

                                            holder.getSendFriendRequestBtn().setVisibility(View.VISIBLE);
                                            holder.getFrndReqPb().setVisibility(View.GONE);
                                            holder.getCancelRequestBtn().setVisibility(View.GONE);

                                        }
                                    }
                                });

                            }
                            else{
                                Toast.makeText(context, context.getString(R.string.unsuccessfully_sent_request,task.getException()), Toast.LENGTH_SHORT).show();

                                holder.getSendFriendRequestBtn().setVisibility(View.VISIBLE);
                                holder.getFrndReqPb().setVisibility(View.GONE);
                                holder.getCancelRequestBtn().setVisibility(View.GONE);
                            }
                    }
                });

            }
        });


        holder.getCancelRequestBtn().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                holder.getCancelRequestBtn().setVisibility(View.GONE);
                holder.getFrndReqPb().setVisibility(View.VISIBLE);

                userId = findFriendModel.getUserId();

                databaseReference.child(currentUser.getUid()).child(userId).child(NodeNames.REQUESTS_TYPE)
                        .setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){

                            databaseReference.child(userId).child(currentUser.getUid()).child(NodeNames.REQUESTS_TYPE)
                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                       // Toast.makeText(context, context.getString(R.string.successfully_sent_request), Toast.LENGTH_SHORT).show();

                                        holder.getSendFriendRequestBtn().setVisibility(View.VISIBLE);
                                        holder.getFrndReqPb().setVisibility(View.GONE);
                                        holder.getCancelRequestBtn().setVisibility(View.GONE);

                                    }
                                    else{

                                        //failed to cancel request
                                        Toast.makeText(context, context.getString(R.string.unsuccessfully_sent_request), Toast.LENGTH_SHORT).show();

                                        holder.getSendFriendRequestBtn().setVisibility(View.GONE);
                                        holder.getFrndReqPb().setVisibility(View.GONE);
                                        holder.getCancelRequestBtn().setVisibility(View.VISIBLE);

                                    }
                                }
                            });

                        }
                        else{
                           // Toast.makeText(context, context.getString(R.string.unsuccessfully_sent_request), Toast.LENGTH_SHORT).show();

                            holder.getSendFriendRequestBtn().setVisibility(View.GONE);
                            holder.getFrndReqPb().setVisibility(View.GONE);
                            holder.getCancelRequestBtn().setVisibility(View.VISIBLE);;
                        }
                    }
                });

            }
        });

    }

    @Override
    public int getItemCount() {
        return frnds.size();
    }


    class FindFriendViewHolder extends RecyclerView.ViewHolder {


        private TextView personName;
        private Button sendFriendRequestBtn, cancelRequestBtn;
        private ProgressBar frndReqPb;
        private CircleImageView image;

        public FindFriendViewHolder(@NonNull View itemView) {

            super(itemView);
            personName = itemView.findViewById(R.id.frnd_Name_req);
            sendFriendRequestBtn = itemView.findViewById(R.id.sendFrndReq);
            cancelRequestBtn = itemView.findViewById(R.id.cancelFrndReq);
            image = itemView.findViewById(R.id.friend_Dp);
            frndReqPb = itemView.findViewById(R.id.progress_req);
        }

        public TextView getPersonName() {
            return personName;
        }

        public Button getSendFriendRequestBtn() {
            return sendFriendRequestBtn;
        }

        public Button getCancelRequestBtn() {
            return cancelRequestBtn;
        }

        public ProgressBar getFrndReqPb() {
            return frndReqPb;
        }

        public CircleImageView getImage() {
            return image;
        }
    }
}

