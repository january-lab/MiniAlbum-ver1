package com.example.huynhxuankhanh.minialbum.fragment;

import android.content.Intent;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.example.huynhxuankhanh.minialbum.R;
import com.example.huynhxuankhanh.minialbum.activity.Main2Activity;
import com.example.huynhxuankhanh.minialbum.activity.MainActivity;
import com.example.huynhxuankhanh.minialbum.adapter.AdapterImageGridView;
import com.example.huynhxuankhanh.minialbum.database.Database;
import com.example.huynhxuankhanh.minialbum.gallary.InfoImage;
import com.example.huynhxuankhanh.minialbum.gallary.LoadFavorite;

/**
 * Created by HUYNHXUANKHANH on 11/17/2017.
 */

public class FragmentFavor extends Fragment{
    private View view;
    private GridView gridView;
    private LoadFavorite loadFavorite;
    private Intent fragFavorIntent;
    private AdapterImageGridView myArrayAdapterGridView;
    private int currentPos = 0;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_picture,container,false);
        gridView = (GridView) view.findViewById(R.id.grd_Image);



        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // send data to activity2: view image full screen
                fragFavorIntent.putExtra("image-info-fav", (Parcelable) loadFavorite.getInfoImage(position));
                // check putExtra is it ok or position is ok ?
                currentPos = gridView.getFirstVisiblePosition();
                startActivity(fragFavorIntent);


            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadFavorite = new LoadFavorite();
        loadFavorite.setDatabase(new Database(getActivity(),"Favorite.sqlite",null,1));
        loadFavorite.loadDataFromDB("SELECT * FROM Favorite");

        myArrayAdapterGridView = new AdapterImageGridView(getActivity(),R.layout.imageview_layout,loadFavorite.getListImage());
        gridView.setAdapter(myArrayAdapterGridView);
        gridView.setSelection(currentPos);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragFavorIntent = new Intent(getActivity(), Main2Activity.class);
        loadFavorite = new LoadFavorite();
        loadFavorite.setDatabase(new Database(getActivity(),"Favorite.sqlite",null,1));
        loadFavorite.loadDataFromDB("SELECT * FROM Favorite");

        myArrayAdapterGridView = new AdapterImageGridView(getActivity(),R.layout.imageview_layout,loadFavorite.getListImage());
      //  gridView.setAdapter(myArrayAdapterGridView);
      //  gridView.setSelection(currentPos);
    }
    public static FragmentFavor newInstance(String StrArg){
        FragmentFavor fragment = new FragmentFavor ();
        Bundle args = new Bundle();
        args.putString("strArg1",StrArg);
        fragment.setArguments(args);
        return fragment;
    }

}