package com.brightfuture.eduquiz.activity;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.brightfuture.eduquiz.Constant;
import com.brightfuture.eduquiz.R;
import com.brightfuture.eduquiz.adapter.TopicAnswerAdapter;
import com.brightfuture.eduquiz.adapter.TopicAnswerAdapter;
import com.brightfuture.eduquiz.helper.AppController;
import com.brightfuture.eduquiz.helper.Session;
import com.brightfuture.eduquiz.helper.Utils;
import com.brightfuture.eduquiz.model.TopicAnswer;
import com.google.android.gms.ads.AdView;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TopicAnswersActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    public ProgressBar progressBar;
    public ArrayList<TopicAnswer> answerList;
    public AdView mAdView;
    public TextView txtBlankList,tvQuestion,tvName,tvLocation,tvDate,etAns;
    public RelativeLayout layout;
    private String strQues,strLoc,user_id,name1;
    String ans;
    TopicAnswerAdapter adapter;

    public SwipeRefreshLayout swipeRefreshLayout;
    public Snackbar snackbar;
    public Toolbar toolbar;
    String name,question,created,question_id,location;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community_answers);
        layout = findViewById(R.id.layout);
        Utils.transparentStatusAndNavigation(TopicAnswersActivity.this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            layout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Topics");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
     /*   mAdView = findViewById(R.id.banner_AdView);
        mAdView.loadAd(new AdRequest.Builder().build());*/

        txtBlankList = findViewById(R.id.txtblanklist);
        progressBar = findViewById(R.id.progressBar);
       // swipeRefreshLayout = findViewById(R.id.swipeLayout);
        recyclerView = findViewById(R.id.rclyt);
        etAns=findViewById(R.id.etAns);
        recyclerView.setLayoutManager(new LinearLayoutManager(TopicAnswersActivity.this));
        txtBlankList.setText("Answer not available");
        tvQuestion = findViewById(R.id.tvQuestion);
        tvName=findViewById(R.id.tvName);
        tvLocation=findViewById(R.id.tvlocation);
        tvDate=findViewById(R.id.tvdate);

        user_id= Session.getUserData(Session.USER_ID, TopicAnswersActivity.this);
        name1=Session.getUserData(Constant.USER_NAME, TopicAnswersActivity.this);



        answerList = new ArrayList<>();
        getData();
        question_id = getIntent().getStringExtra(Constant.ID);
        created = getIntent().getStringExtra(Constant.dt_date);
        name = getIntent().getStringExtra(Constant.dt_name);
        question = getIntent().getStringExtra(Constant.dt_description);

        tvQuestion.setText(name);
        tvName.setText(question);
        tvLocation.setText(created.substring(0,10));
        tvDate.setVisibility(View.GONE);
    /*    swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                answerList.clear();
                getData();
                swipeRefreshLayout.setRefreshing(false);
            }
        });*/
    }

    private void  getData(){
        progressBar.setVisibility(View.VISIBLE);
        if (Utils.isNetworkAvailable(TopicAnswersActivity.this)) {
            getSubCategoryFromJson();

        } else {
            setSnackBar();
            progressBar.setVisibility(View.GONE);
        }
    }

    public void getSubCategoryFromJson() {
        progressBar.setVisibility(View.VISIBLE);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constant.QUIZ_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {

                            JSONObject jsonObject = new JSONObject(response);
                            boolean error = jsonObject.getBoolean(Constant.ERROR);

                            if (!error) {
                                JSONArray jsonArray = jsonObject.getJSONArray(Constant.DATA);
                                answerList.clear();
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    TopicAnswer ans = new TopicAnswer();
                                    JSONObject object = jsonArray.getJSONObject(i);
                                    ans.setDa_answer(object.getString(Constant.da_answer));
                                    ans.setDa_created(object.getString(Constant.da_created));
                                    ans.setDa_location(object.getString(Constant.da_location));
                                    ans.setU_id(object.getString(Constant.CQUID));
                                    ans.setU_name(object.getString(Constant.CQUSERNAME));
                                    ans.setDa_id(object.getString(Constant.da_id));


                                        answerList.add(ans);

                                }
                                if (answerList.size() == 0) {
                                    txtBlankList.setVisibility(View.VISIBLE);
                                    txtBlankList.setText("Answer not available");
                                }
                                adapter = new TopicAnswerAdapter(answerList, TopicAnswersActivity.this);
                                recyclerView.setAdapter(adapter);
                                progressBar.setVisibility(View.GONE);
                            }/*else{
                                if(Constant.TotalLevel!=0){
                                    Intent intent = new Intent(SubcategoryActivity.this, LevelActivity.class);
                                    intent.putExtra("fromQue", "subCate");
                                    startActivity(intent);
                                    ((SubcategoryActivity)getApplicationContext()).finish();
                                }
                            }*/
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        progressBar.setVisibility(View.GONE);
                    }
                }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put(Constant.accessKey, Constant.accessKeyValue);
                params.put(Constant.get_answers_by_topic_id, "1");
                params.put(Constant.da_id, question_id);

                System.out.println("params  " + params.toString());
                return params;
            }
        };
        AppController.getInstance().getRequestQueue().getCache().clear();
        AppController.getInstance().addToRequestQueue(stringRequest);
    }


    public void setSnackBar() {
        snackbar = Snackbar
                .make(findViewById(android.R.id.content), getString(R.string.msg_no_internet), Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(R.string.retry), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        getData();
                    }
                });

        snackbar.show();
    }

    public void onPost(View view) {
        ans = etAns.getText().toString();

        if(TextUtils.isEmpty(ans))
        {
            etAns.setError("Please answer");
        }
        else
        {
            etAns.setError(null);
            postAnswer();


        }
    }


    public void postAnswer() {
        //  progressBar.setVisibility(View.VISIBLE);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constant.QUIZ_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //progressBar.setVisibility(View.GONE);
                        try {

                            JSONObject jsonObject = new JSONObject(response);
                            boolean error = jsonObject.getBoolean(Constant.ERROR);

                            if (!error) {
                                Log.d("ADDCommunityACTIVITY","Added Answer");
                                Toast.makeText(TopicAnswersActivity.this,"Answer Added Succesfully",Toast.LENGTH_SHORT).show();

                                getData();
                                etAns.setText("");
                              /* TopicAnswer ca= new TopicAnswer();
                               ca.setCa_answer(ans);
                               ca.setU_name(name1);
                               ca.setCa_created("now");
                                answerList.add(ca);
                                adapter.notifyDataSetChanged();*/
                                /*JSONArray jsonArray = jsonObject.getJSONArray(Constant.DATA);
                                Intent intent2 = new Intent(AddQuestionActivity.this, CommunityActivity.class);

                                startActivity(intent2);*/

                               /* for (int i = 0; i < jsonArray.length(); i++) {
                                    SubCategory subCate = new SubCategory();
                                    JSONObject object = jsonArray.getJSONObject(i);
                                    subCate.setId(object.getString(Constant.ID));
                                    subCate.setCategoryId(object.getString(Constant.MAIN_CATE_ID));
                                    subCate.setCategory_name(object.getString(Constant.CATEGORY_NAME));
                                    subCate.setName(object.getString(Constant.SUB_CATE_NAME));
                                    subCate.setImage(object.getString(Constant.IMAGE));
                                    subCate.setStatus(object.getString(Constant.STATUS));
                                    subCate.setMaxLevel(object.getString(Constant.MAX_LEVEL));

                                    subCate.setQuestion_count(object.getString(Constant.QUES_COUNT));
                                    int count= Integer.parseInt(object.getString(Constant.QUES_COUNT));
                                    if(count>0) {

                                    }
                                }*/
                                /*overridePendingTransition(R.anim.open_next, R.anim.close_next);
                                finish();*/

                            }else{

                                Toast.makeText(TopicAnswersActivity.this,"Something went wrong. Please try again.",Toast.LENGTH_SHORT).show();
                                /*overridePendingTransition(R.anim.open_next, R.anim.close_next);
                                finish();*/
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(TopicAnswersActivity.this,"Something went wrong. Please try again.",Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        //  progressBar.setVisibility(View.GONE);
                    }
                }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put(Constant.accessKey, Constant.accessKeyValue);
                params.put(Constant.add_topic_answer, "1");
                params.put(Constant.ID, question_id);
                params.put(Constant.ANSWER, ans);
                params.put(Constant.USER_ID, user_id);
                params.put(Constant.USER_NAME, name1);
                params.put(Constant.STATUS, "Active");
                params.put(Constant.TYPE, "normal");
                params.put(Constant.LOCATION, "");




                System.out.println("params  " + params.toString());
                return params;
            }
        };
        AppController.getInstance().getRequestQueue().getCache().clear();
        AppController.getInstance().addToRequestQueue(stringRequest);
    }


    @Override
    public void onPause() {
        super.onPause();
        if (snackbar != null)
            snackbar.dismiss();
    }

    @Override
    public void onResume() {
        super.onResume();
    }




}