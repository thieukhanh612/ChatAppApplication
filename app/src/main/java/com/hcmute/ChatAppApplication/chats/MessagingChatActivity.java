package com.hcmute.ChatAppApplication.chats;

import static android.provider.MediaStore.ACTION_IMAGE_CAPTURE;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;

import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.hcmute.ChatAppApplication.R;
import com.hcmute.ChatAppApplication.common.Constants;
import com.hcmute.ChatAppApplication.common.NodeNames;
import com.hcmute.ChatAppApplication.common.Util;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipDrawable;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.mlkit.nl.smartreply.SmartReply;
import com.google.mlkit.nl.smartreply.SmartReplyGenerator;
import com.google.mlkit.nl.smartreply.SmartReplySuggestion;
import com.google.mlkit.nl.smartreply.SmartReplySuggestionResult;
import com.google.mlkit.nl.smartreply.TextMessage;

import java.io.ByteArrayOutputStream;
import java.io.File;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MessagingChatActivity extends AppCompatActivity {
    //defined page of number of page load message from database
    private static final int REECORD_PER_PAGE = 30;
    //defined request code for pick image
    private static final int REQUEST_FOR_PICK_IMAGE = 101;
    //defined request code for capture image
    private static final int REQUEST_FOR_CAPTURE_IMAGE = 102;
    //defined request code for pick video
    private static final int REQUEST_FOR_PICK_VIDEO = 103;
    //defined request code for download file
    private static final int REQUEST_FOR_DOWNLOAD_FILE = 104;
    //declare linear layout for chat
    private LinearLayout llChat, llFileUploadingStatus;
    //declare edix text view for message
    private EditText entrMsg;
    //declare image view for send, attach or profile
    private ImageView sendIv, attachIv, ivProfile;
    //declare text view for user name and status
    private TextView tvUserName, tvUserStatus;
    //declare firebase authorization
    private FirebaseAuth firebaseAuth;
    //declare database from firebase
    private FirebaseDatabase rootRefrence;
    //declare id of user, name, photo name
    private String currentUserId, chatUserId, photoNameUser, userName;
    //declare view for message
    private RecyclerView recyclerViewMessage;
    //declare layout of refresh
    private SwipeRefreshLayout swipeRefreshLayout;
    //declare dialog
    private BottomSheetDialog bottomSheetDialog;
    //declare group chat
    private ChipGroup smartRepliesCgp;
    //declare list of message
    private List<TextMessage> conversation;
    //defined number of page message
    private int countPage = 1;
    //declare list of message model
    private ArrayList<MessageModel> messagesList;
    //declare message adapter
    private MessageAdapter messageAdapter;
    //declare database reference
    private DatabaseReference messageReference;
    //declare listener of event
    private ChildEventListener childEventListener;

    //action on create
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging_chat);

        if (getIntent().hasExtra(Constants.USERID_INTENT_KEY)) {
            chatUserId = getIntent().getStringExtra(Constants.USERID_INTENT_KEY);

        }

        if (getIntent().hasExtra(Constants.USER_NAME_INTENT_KEY)) {
            userName = getIntent().getStringExtra(Constants.USER_NAME_INTENT_KEY);
        }

        if (getIntent().hasExtra(Constants.USER_PHOTO_INTENT_KEY)) {
            photoNameUser = getIntent().getStringExtra(Constants.USER_PHOTO_INTENT_KEY);
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {

            actionBar.setTitle("");

            ViewGroup viewGrp = (ViewGroup) getLayoutInflater().inflate(R.layout.custom_action_bar, null);
            ivProfile = viewGrp.findViewById(R.id.ivProfileCab);
            tvUserName = viewGrp.findViewById(R.id.tvUserNameCab);
            tvUserStatus = viewGrp.findViewById(R.id.tvUserStatusCab);

            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setElevation(0);

            actionBar.setCustomView(viewGrp);

        }


        llChat = findViewById(R.id.sentMessageChatLL);
        llFileUploadingStatus = findViewById(R.id.fileProgressLL);
        entrMsg = findViewById(R.id.edtMessage);
        sendIv = findViewById(R.id.ivSend);
        attachIv = findViewById(R.id.ivAttach);
        recyclerViewMessage = findViewById(R.id.messagingRv);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshMessage);
        smartRepliesCgp = findViewById(R.id.cgSmartReply);

        conversation = new ArrayList<>();

        firebaseAuth = FirebaseAuth.getInstance();
        rootRefrence = FirebaseDatabase.getInstance();
        currentUserId = firebaseAuth.getCurrentUser().getUid();
        rootRefrence.getReference().child(NodeNames.CHAT).child(currentUserId).child(chatUserId).child(NodeNames.UNREAD_COUNT).setValue("0");

//Cab data
        tvUserName.setText(userName);
        DatabaseReference userSpecificRef = rootRefrence.getReference().child(NodeNames.USERS).child(chatUserId);

        userSpecificRef.addValueEventListener(new ValueEventListener() {
            @Override
            //action when data change
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String status = " ";
                if (snapshot.child(NodeNames.ONLINE).getValue() != null) {
                    status = snapshot.child(NodeNames.ONLINE).getValue().toString();
                }
                if (status.equals("true")) {
                    tvUserStatus.setText(Constants.ONLINE);
                } else {
                    tvUserStatus.setText(" ");
                }
            }

            @Override
            //action when get an error from database
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        if (!TextUtils.isEmpty(photoNameUser)) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(Constants.IMAGE_FOLDER).child(photoNameUser);

            storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                //action when success get message from database
                public void onSuccess(Uri uri) {
                    Glide.with(MessagingChatActivity.this)

                            .load(uri)
                            .placeholder(R.drawable.profile_image_default)
                            .error(R.drawable.profile_image_default)
                            .into(ivProfile);
                }
            });
        }


        //Adapter
        messagesList = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, messagesList);

        recyclerViewMessage.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewMessage.setAdapter(messageAdapter);


        loadMessage();

        recyclerViewMessage.scrollToPosition(messagesList.size() - 1);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            //action when refresh conversation
            public void onRefresh() {
                countPage++;
                loadMessage();
            }
        });

        // set event when click send message
        sendIv.setOnClickListener(new View.OnClickListener() {
            @Override
            //action on click send
            public void onClick(View view) {

                DatabaseReference mRootref = rootRefrence.getReference().child(NodeNames.MESSAGES).child(currentUserId).child(chatUserId).push();
                String pushId = mRootref.getKey();

                sendMessage(entrMsg.getText().toString(), Constants.MESSAGE_TYPE_TEXT, pushId);
            }
        });

        bottomSheetDialog = new BottomSheetDialog(this);

        View view = getLayoutInflater().inflate(R.layout.chat_file_share_options, null);        //Setting the view for bottomSheetDialog
        view.findViewById(R.id.cameraLL).setOnClickListener(new View.OnClickListener() {
            @Override
            //action when click send file button
            public void onClick(View view) {

                bottomSheetDialog.dismiss();
                Intent cameraIntent = new Intent(ACTION_IMAGE_CAPTURE);
                startActivityIfNeeded(cameraIntent, REQUEST_FOR_CAPTURE_IMAGE);

            }
        });

        view.findViewById(R.id.galleryLL).setOnClickListener(new View.OnClickListener() {
            @Override
            //action when click gallery button
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
                Intent imageIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityIfNeeded(imageIntent, REQUEST_FOR_PICK_IMAGE);
            }
        });

        view.findViewById(R.id.videoLL).setOnClickListener(new View.OnClickListener() {
            @Override
            //action when click video
            public void onClick(View view) {

                bottomSheetDialog.dismiss();
                Intent videoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                startActivityIfNeeded(videoIntent, REQUEST_FOR_PICK_VIDEO);
            }
        });

        view.findViewById(R.id.cancelLL).setOnClickListener(new View.OnClickListener() {
            @Override
            //action when click cancel
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
            }
        });

        bottomSheetDialog.setContentView(view);
        //set event when click attach button
        attachIv.setOnClickListener(new View.OnClickListener() {
            @Override
            //action when click attach button
            public void onClick(View view) {

                if (ActivityCompat.checkSelfPermission(MessagingChatActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

                    if (bottomSheetDialog != null) {
                        bottomSheetDialog.show();
                    }

                } else {

                    ActivityCompat.requestPermissions(MessagingChatActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                }
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (inputMethodManager != null) {
                    inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }

            }
        });
        //set event of message
        entrMsg.addTextChangedListener(new TextWatcher() {
            @Override
            //action before text change
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            //action when change text
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            //action after changing text
            public void afterTextChanged(Editable editable) {

                DatabaseReference currentUserRef = FirebaseDatabase.getInstance().getReference()
                        .child(NodeNames.CHAT)
                        .child(currentUserId)
                        .child(chatUserId);

                if (editable.toString().matches("")) {
                    currentUserRef.child(NodeNames.TYPING).setValue(Constants.TYPING_STOPPED);
                } else {
                    currentUserRef.child(NodeNames.TYPING).setValue(Constants.TYPING_STARTED);
                }

                DatabaseReference chatUserRef = FirebaseDatabase.getInstance().getReference()
                        .child(NodeNames.CHAT)
                        .child(chatUserId)
                        .child(currentUserId);

                chatUserRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    //action when data change
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.child(NodeNames.TYPING).getValue() != null) {
                            String typingStatus = snapshot.child(NodeNames.TYPING).getValue().toString();
                            if (typingStatus.equals(Constants.TYPING_STARTED)) {
                                tvUserStatus.setText(Constants.STATUS_TYPING);
                            } else {
                                tvUserStatus.setText(Constants.ONLINE);
                            }
                        }
                    }

                    @Override
                    //action when get an error from database
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


            }
        });


    }
    //func send message
    private void sendMessage(String msg, String msgType, String pushId) {

        try {

            if (!msg.equals("")) {

                HashMap messageMap = new HashMap();

                messageMap.put(NodeNames.MESSAGE_ID, pushId);
                messageMap.put(NodeNames.MESSAGE, msg);
                messageMap.put(NodeNames.MESSAGE_TYPE, msgType);
                messageMap.put(NodeNames.FROM, currentUserId);
                messageMap.put(NodeNames.TIME, ServerValue.TIMESTAMP);

                String currentUserRef = NodeNames.MESSAGES + "/" + currentUserId + "/" + chatUserId;
                String chatUserRef = NodeNames.MESSAGES + "/" + chatUserId + "/" + currentUserId;

                HashMap messageUserHashMap = new HashMap();

                messageUserHashMap.put(currentUserRef + "/" + pushId, messageMap);
                messageUserHashMap.put(chatUserRef + "/" + pushId, messageMap);

                entrMsg.setText("");

                if(msgType.equals(Constants.MESSAGE_TYPE_TEXT)){
                    conversation.add(TextMessage.createForLocalUser(msg,System.currentTimeMillis()));
                }

                rootRefrence.getReference().updateChildren(messageUserHashMap, new DatabaseReference.CompletionListener() {
                    @Override
                    //action when complete send message
                    public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                        if (error != null) {
                            Toast.makeText(MessagingChatActivity.this, getString(R.string.message_sent_failed, error.getMessage()), Toast.LENGTH_SHORT).show();
                        } else {

                            Toast.makeText(MessagingChatActivity.this, getString(R.string.sent_message), Toast.LENGTH_SHORT).show();

                            String tittle = "New Message";
                            Util.sendNotification(MessagingChatActivity.this, msg, tittle, chatUserId);
                            Util.updateChatDetails(MessagingChatActivity.this, currentUserId, chatUserId, msg);
                        }
                    }
                });
            }

        } catch (Exception e) {
            Toast.makeText(MessagingChatActivity.this, getString(R.string.message_sent_failed, e.getMessage()), Toast.LENGTH_SHORT).show();
            //e.printStackTrace();
        }

    }

    //func load message from database
    private void loadMessage() {

        messagesList.clear();
        conversation.clear();
        smartRepliesCgp.removeAllViews();

        messageReference = rootRefrence.getReference().child(NodeNames.MESSAGES).child(currentUserId).child(chatUserId);

        Query meessageQuery = messageReference.limitToLast(countPage * REECORD_PER_PAGE);

        if (childEventListener != null) {
            meessageQuery.removeEventListener(childEventListener);
        }

        childEventListener = new ChildEventListener() {
            @Override
            //action when send message
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                MessageModel messageModel = snapshot.getValue(MessageModel.class);

                messagesList.add(messageModel);
                messageAdapter.notifyDataSetChanged();

                recyclerViewMessage.scrollToPosition(messagesList.size() - 1);
                swipeRefreshLayout.setRefreshing(false);
                showSmartReply(messageModel);

            }

            @Override
            //action when change message
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            //action when remove message
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                loadMessage();

            }

            @Override
            //action when move message
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            //action when get error from database
            public void onCancelled(@NonNull DatabaseError error) {
                swipeRefreshLayout.setRefreshing(false);
            }
        };
        meessageQuery.addChildEventListener(childEventListener);

    }


    @Override
    //action when request server
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {

            if (grantResults.length > 0 && permissions[0] == Manifest.permission.READ_EXTERNAL_STORAGE) {

                if (bottomSheetDialog != null) {
                    bottomSheetDialog.show();
                } else {
                    Toast.makeText(MessagingChatActivity.this, getApplicationContext().getString(R.string.permission_denied), Toast.LENGTH_SHORT).show();

                }
            }

        }
    }

    @Override
    //action when receive result
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            if (requestCode == REQUEST_FOR_CAPTURE_IMAGE) {

                Bitmap bitmap = (Bitmap) data.getExtras().get("data");

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                uploadBytes(byteArrayOutputStream, Constants.MESSAGE_TYPE_IMAGE);

            } else if (requestCode == REQUEST_FOR_PICK_IMAGE) {
                Uri uri = data.getData();
                uploadFileToServer(uri, Constants.MESSAGE_TYPE_IMAGE);

            } else if (requestCode == REQUEST_FOR_PICK_VIDEO) {
                Uri uri = data.getData();
                uploadFileToServer(uri, Constants.MESSAGE_TYPE_VIDEO);

            }
        }
    }

    @Override
    //action when click menu item
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home:
                finish();
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }
    //func to upload file to server
    private void uploadFileToServer(Uri uri, String messageType) {

        DatabaseReference reference = rootRefrence.getReference().child(NodeNames.MESSAGES).child(currentUserId).child(chatUserId).push();
        String pushId = reference.getKey();

        String folderName = messageType.equals(Constants.MESSAGE_TYPE_VIDEO) ? Constants.MESSAGE_VIDEO_FOLDER : Constants.MESSAGE_IMAGE_FOLDER;
        String fileName = messageType.equals(Constants.MESSAGE_TYPE_VIDEO) ? pushId + ".mp4" : pushId + ".jpg";

        StorageReference mReference = FirebaseStorage.getInstance().getReference().child(folderName).child(fileName);
        UploadTask uploadTask = mReference.putFile(uri);
        uploadProgress(uploadTask, mReference, pushId, messageType);
    }
    //func to upload bytes of file
    private void uploadBytes(ByteArrayOutputStream byteArrayOutputStream, String messageType) {

        DatabaseReference reference = rootRefrence.getReference().child(NodeNames.MESSAGES).child(currentUserId).child(chatUserId).push();
        String pushId = reference.getKey();

        String folderName = messageType.equals(Constants.MESSAGE_TYPE_VIDEO) ? Constants.MESSAGE_VIDEO_FOLDER : Constants.IMAGE_FOLDER;
        String fileName = messageType.equals(Constants.MESSAGE_TYPE_VIDEO) ? pushId + ".mp4" : pushId + ".jpg";

        StorageReference mReference = FirebaseStorage.getInstance().getReference().child(folderName).child(fileName);

        UploadTask uploadTask = mReference.putBytes(byteArrayOutputStream.toByteArray());
        uploadProgress(uploadTask, mReference, pushId, messageType);

    }
    //func to get upload progress
    private void uploadProgress(final UploadTask uploadTask, StorageReference storageReferencePath, String pushId, String messageType) {

        View view = getLayoutInflater().inflate(R.layout.file_upload_status, null);
        final TextView uploadTittleTv = view.findViewById(R.id.uploadTittletextView);
        ProgressBar progressBar = view.findViewById(R.id.pbUploadProcessStatus);
        ImageView ivPause = view.findViewById(R.id.ivPause);
        ImageView ivResume = view.findViewById(R.id.ivResume);
        ImageView ivCancel = view.findViewById(R.id.ivCancelDownloadOrUpload);
        llFileUploadingStatus.addView(view);
        //set event to pause button
        ivPause.setOnClickListener(new View.OnClickListener() {
            @Override
            //func when click pause
            public void onClick(View view) {
                uploadTask.pause();
                ivResume.setVisibility(View.VISIBLE);
                ivPause.setVisibility(View.GONE);
            }
        });
        //set event to resume button
        ivResume.setOnClickListener(new View.OnClickListener() {
            @Override
            //action when click resume button
            public void onClick(View view) {
                uploadTask.resume();
                ivResume.setVisibility(View.GONE);
                ivPause.setVisibility(View.VISIBLE);
            }
        });
        //set event on cancel button
        ivCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            //action when click cancel
            public void onClick(View view) {
                uploadTask.cancel();
            }
        });


        uploadTittleTv.setText(getString(R.string.uploadStatus, messageType, "0"));
        //set when in progress upload
        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            //action when in progress
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {

                double progressCount = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();

                progressBar.setProgress((int) progressCount);
                uploadTittleTv.setText(getString(R.string.uploadStatus, messageType, String.valueOf(progressBar.getProgress())));

            }
        });
        //set event on upload task when complete
        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            //action when complete upload
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                llFileUploadingStatus.removeView(view);
                if (task.isSuccessful()) {
                    storageReferencePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String downloadUrl = uri.toString();
                            sendMessage(downloadUrl, messageType, pushId);
                        }
                    });
                }
            }
        });
        //set event on upload task when fail
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            //action when fail to upload
            public void onFailure(@NonNull Exception e) {
                llFileUploadingStatus.removeView(view);
                Toast.makeText(MessagingChatActivity.this, getString(R.string.failed_to_upload, e.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });

    }
    //func to delete message
    public void deleteMessage(String messageId, String messageType) {

        DatabaseReference ref = rootRefrence.getReference().child(NodeNames.MESSAGES).child(currentUserId).child(chatUserId).child(messageId);
        //set event on remove message
        ref.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            //action when complete remove message
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    DatabaseReference chatref = rootRefrence.getReference().child(NodeNames.MESSAGES).child(chatUserId).child(currentUserId).child(messageId);
                   //set event when remove message on database
                    chatref.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        //action when remove message on database
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {

                                if (!messageType.equals(Constants.MESSAGE_TYPE_TEXT)) {
                                    StorageReference storageReference = FirebaseStorage.getInstance().getReference();
                                    String folder = messageType.equals(Constants.MESSAGE_TYPE_VIDEO) ? Constants.MESSAGE_VIDEO_FOLDER :
                                            Constants.IMAGE_FOLDER;

                                    String fileName = messageType.equals(Constants.MESSAGE_TYPE_VIDEO) ? messageId + ".mp4" :
                                            messageId + ".jpg";
                                    //set event when remove file in database
                                    storageReference.child(folder).child(fileName).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        //action when complete remove file on database
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (!task.isSuccessful()) {
                                                Toast.makeText(MessagingChatActivity.this, getString(R.string.failed_to_delete, task.getException()), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }

                            } else {
                                Toast.makeText(MessagingChatActivity.this, getString(R.string.failed_to_delete, task.getException()), Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
                } else {
                    Toast.makeText(MessagingChatActivity.this, getString(R.string.failed_to_delete, task.getException()), Toast.LENGTH_SHORT).show();
                }

            }
        });

    }
    //func to download file from database
    public void downloadFile(String messageId, String messageType, boolean isShared) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_FOR_DOWNLOAD_FILE);
        } else {


            String folder = messageType.equals(Constants.MESSAGE_TYPE_VIDEO) ? Constants.MESSAGE_VIDEO_FOLDER :
                    Constants.MESSAGE_IMAGE_FOLDER;

            String fileName = messageType.equals(Constants.MESSAGE_TYPE_VIDEO) ? messageId + ".mp4" :
                    messageId + ".jpg";

            final String localFilePath = getExternalFilesDir(null).getAbsolutePath() + "/" + fileName;
            Log.d("local", localFilePath);
            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(folder).child(fileName);
            File localFile = new File(localFilePath);

            //set event when get url from database
            storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                //action when success get url from database
                public void onSuccess(Uri uri) {
                    try {
                        if (localFile.exists() || localFile.createNewFile()) {

                            FileDownloadTask downloadTask = storageReference.getFile(localFile);

                            View view = getLayoutInflater().inflate(R.layout.file_upload_status, null);
                            final TextView uploadTittleTv = view.findViewById(R.id.uploadTittletextView);
                            ProgressBar progressBar = view.findViewById(R.id.pbUploadProcessStatus);
                            ImageView ivPause = view.findViewById(R.id.ivPause);
                            ImageView ivResume = view.findViewById(R.id.ivResume);
                            ImageView ivCancel = view.findViewById(R.id.ivCancelDownloadOrUpload);

                            llFileUploadingStatus.addView(view);
                            //set event to pause button
                            ivPause.setOnClickListener(new View.OnClickListener() {
                                @Override
                                //action when click pause
                                public void onClick(View view) {
                                    downloadTask.pause();
                                    ivResume.setVisibility(View.VISIBLE);
                                    ivPause.setVisibility(View.GONE);
                                }
                            });
                            //set event to resume button
                            ivResume.setOnClickListener(new View.OnClickListener() {
                                @Override
                                //action when click resume
                                public void onClick(View view) {
                                    downloadTask.resume();
                                    ivResume.setVisibility(View.GONE);
                                    ivPause.setVisibility(View.VISIBLE);
                                }
                            });
                            //set event to cancel button
                            ivCancel.setOnClickListener(new View.OnClickListener() {
                                @Override
                                //action when click cancel
                                public void onClick(View view) {
                                    downloadTask.cancel();
                                    llFileUploadingStatus.removeView(view);
                                }
                            });

                            uploadTittleTv.setText(getString(R.string.downloadStatus, messageType, "0"));
                            //set event on download task
                            downloadTask.addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                                @Override
                                //action when in progress download
                                public void onProgress(@NonNull FileDownloadTask.TaskSnapshot snapshot) {
                                    double progressCount = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();

                                    progressBar.setProgress((int) progressCount);
                                    uploadTittleTv.setText(getString(R.string.downloadStatus, messageType, String.valueOf(progressBar.getProgress())));
                                }
                            });
                            //set event when complete download task
                            downloadTask.addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
                                @Override
                                //action when complete
                                public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
                                    llFileUploadingStatus.removeView(view);
                                    if (task.isSuccessful()) {
                                        //set event on suceesful get download url from storage
                                        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            //action when success get url from storage
                                            public void onSuccess(Uri uri) {

                                                if (isShared) {
                                                    Intent intentShared = new Intent();
                                                    intentShared.setAction(Intent.ACTION_SEND);
                                                    intentShared.putExtra(Intent.EXTRA_STREAM, Uri.parse(localFilePath));
                                                    if (messageType.equals(Constants.MESSAGE_TYPE_VIDEO)) {
                                                        intentShared.setDataAndType(uri, "video/mp4");
                                                    } else if (messageType.equals(Constants.MESSAGE_TYPE_IMAGE)) {
                                                        intentShared.setDataAndType(uri, "image/jpg");
                                                    }
                                                    startActivity(Intent.createChooser(intentShared, getString(R.string.share_with)));
                                                } else {
                                                    Snackbar snackbar = Snackbar.make(llFileUploadingStatus, getString(R.string.downloaded_succesfully),
                                                            Snackbar.LENGTH_INDEFINITE);
                                                    //set event to snackbar
                                                    snackbar.setAction(R.string.view, new View.OnClickListener() {
                                                        @Override
                                                        //action when click snackbar
                                                        public void onClick(View view) {
                                                            Uri uri = Uri.parse(localFilePath);
                                                            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                                            if (messageType.equals(Constants.MESSAGE_TYPE_VIDEO)) {
                                                                intent.setDataAndType(uri, "video/mp4");
                                                            } else if (messageType.equals(Constants.MESSAGE_TYPE_IMAGE)) {
                                                                intent.setDataAndType(uri, "image/jpg");
                                                            }
                                                            startActivity(intent);
                                                        }
                                                    });
                                                    snackbar.show();
                                                }
                                            }
                                        });
                                    }
                                }
                            });
                            //set event on fail to download task
                            downloadTask.addOnFailureListener(new OnFailureListener() {
                                @Override
                                //action when fail
                                public void onFailure(@NonNull Exception e) {
                                    llFileUploadingStatus.removeView(view);
                                    Toast.makeText(MessagingChatActivity.this, getString(R.string.failed_to_download, e.getMessage()), Toast.LENGTH_SHORT)
                                            .show();
                                }
                            });
                        }
                    } catch (Exception e) {

                    }
                }
            });
        }

    }

    @Override
    //action on click back
    public void onBackPressed() {
        rootRefrence.getReference().child(NodeNames.CHAT).child(currentUserId).child(chatUserId).child(NodeNames.UNREAD_COUNT).setValue("0");
        super.onBackPressed();
    }
    //func to show smart reply
    private void showSmartReply(MessageModel messageModel) {

        conversation.clear();
        smartRepliesCgp.removeAllViews();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                .child(NodeNames.MESSAGES).child(currentUserId).child(chatUserId);

        Query lastMessageQuery = databaseReference.orderByKey().limitToLast(1);
        //set event when last message change
        lastMessageQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            //action when message change
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot data: snapshot.getChildren()){
                    MessageModel message = data.getValue(MessageModel.class);

                    if(message.getFrom().equals(chatUserId) && messageModel.getMessage_id().equals(message.getMessage_id())){

                        conversation.add(TextMessage.createForRemoteUser(message.getMessage(),System.currentTimeMillis(),chatUserId));
                        if(!conversation.isEmpty()){
                            SmartReplyGenerator smartReply = SmartReply.getClient();
                            //set event when success suggest replies
                            smartReply.suggestReplies(conversation).addOnSuccessListener(new OnSuccessListener<SmartReplySuggestionResult>() {
                                @Override
                                //action when success suggest replies
                                public void onSuccess(SmartReplySuggestionResult smartReplySuggestionResult) {

                                    if(smartReplySuggestionResult.getStatus()==SmartReplySuggestionResult.STATUS_SUCCESS){
                                        for(SmartReplySuggestion suggestion: smartReplySuggestionResult.getSuggestions()){

                                            String replyText = suggestion.getText();
                                            Log.d("smartReply", replyText);

                                            Chip chip = new Chip(MessagingChatActivity.this);

                                            ChipDrawable chipDrawable = ChipDrawable.createFromAttributes(MessagingChatActivity.this,null,
                                                    0,R.style.Widget_MaterialComponents_Chip_Action);

                                            chip.setChipDrawable(chipDrawable);

                                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                                    LinearLayout.LayoutParams.WRAP_CONTENT
                                            );

                                            params.setMargins(16,16,16,16);

                                            chip.setLayoutParams(params);
                                            chip.setText(replyText);
                                            chip.setTag(replyText);

                                            chip.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    DatabaseReference messageRef = FirebaseDatabase.getInstance().getReference()
                                                            .child(NodeNames.MESSAGES)
                                                            .child(currentUserId)
                                                            .child(chatUserId)
                                                            .push();

                                                    String newMessageId = messageRef.getKey();
                                                    sendMessage(view.getTag().toString(),Constants.MESSAGE_TYPE_TEXT,newMessageId);
                                                }
                                            });
                                            smartRepliesCgp.addView(chip);
                                        }
                                    }
                                }
                                //action when fail to suggest
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                }
                            });
                        }
                    }
                }
            }

            @Override
            //action when get error from database
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });





    }
}