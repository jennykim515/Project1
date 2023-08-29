package com.example.project1;
import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Collections;


class Pair {
    // Pair attributes
    public int i;
    public int j;
    public int id;

    // Constructor to initialize pair
    public Pair(int i, int j) {
        // This keyword refers to current instance
        this.i = i;
        this.j = j;
        this.id = i * 12 + j;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + i;
        result = 31 * result + j;
        return result;
    }

    // shallow equals
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Pair)) {
            return false;
        }
        Pair other = (Pair) obj;
        return other.i == i && other.j == j;
    }

}

public class MainActivity extends AppCompatActivity {
    private static final int COLUMN_COUNT = 10;
    private static final int ROW_COUNT = 12;
    private int clock = 0;
    private static final int NUM_MINES = 10;

    GameStatus gameStatus = GameStatus.NOTSTARTED;
    Set<Pair> mines;
    int[][] neighbors = { {-1,-1}, {-1,0}, {-1,1}, {1,-1}, {1,0}, {1,1}, {0,-1}, {0,1} };

    private ArrayList<TextView> cell_tvs;

    private int dpToPixel(int dp) {
        float density = Resources.getSystem().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private int findIndexOfCellTextView(TextView tv) {
        for (int n=0; n<cell_tvs.size(); n++) {
            if (cell_tvs.get(n) == tv)
                return n;
        }
        return -1;
    }

    private void gameOver() {
        gameStatus = GameStatus.GAMEOVER;
        clock = 0;
        // TODO: reveal mines
        GridLayout gridLayout = findViewById(R.id.square);
        for (Pair mine: mines) {
            TextView tv = gridLayout.findViewById(mine.id);
            tv.setBackgroundColor(Color.RED);
        }
    }

    public void onClickTV(View view){
        switch (gameStatus) {
            case NOTSTARTED:
                gameStatus = GameStatus.STARTED;
                break;

            case GAMEOVER:
                // show game over
                break;
        }

        TextView tv = (TextView) view;
        int n = findIndexOfCellTextView(tv);
        int i = n/COLUMN_COUNT;
        int j = n%COLUMN_COUNT;

        // if mine is selected
        if (mines.contains(new Pair(i,j))) {
            Log.d("myTag", "MINE SELECTED");
            tv.setBackgroundColor(Color.RED);
            gameOver();
            return;
        }

        tv.setTextColor(Color.GREEN);

        int adjacentMines = 0;
        for (int[] neighbor : neighbors) {
            if (mines.contains(new Pair(neighbor[0] + i, neighbor[1] + j))) {
                adjacentMines++;
            }
        }

        if (adjacentMines > 0) {
            tv.setText(String.valueOf(adjacentMines));
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cell_tvs = new ArrayList<>();
        mines = randomMineGenerator();

        GridLayout grid = findViewById(R.id.square);

        for (int i = 0; i < ROW_COUNT; i++) {
            for (int j = 0; j < COLUMN_COUNT; j++) {
                TextView tv = new TextView(this);
                tv.setHeight( dpToPixel(24) );
                tv.setWidth( dpToPixel(24) );
                tv.setTextSize(20);
                tv.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
                tv.setTextColor(Color.GRAY);
                tv.setBackgroundColor(Color.GRAY);
                tv.setId(i*12 + j);
                tv.setOnClickListener(this::onClickTV);

                GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
                lp.setMargins(dpToPixel(2), dpToPixel(2), dpToPixel(2), dpToPixel(2));
                lp.rowSpec = GridLayout.spec(i);
                lp.columnSpec = GridLayout.spec(j);

                grid.addView(tv, lp);
                cell_tvs.add(tv);
            }
        }
        runTimer();
    }

    // timer logic
    private void runTimer() {
        final TextView timeView = (TextView) findViewById(R.id.timer);

        final Handler handler = new Handler();

        handler.post(new Runnable() {
            @Override
            public void run() {
                int hours =clock/3600;
                int minutes = (clock%3600) / 60;
                int seconds = clock%60;
                String time = String.format("%d:%02d:%02d", hours, minutes, seconds);

                if (gameStatus == GameStatus.STARTED) {
                    clock++;
                }
                handler.postDelayed(this, 1000);
                timeView.setText(time);
            }
        });
    }

    // random mine generator logic
    private Set<Pair> randomMineGenerator() {
        ArrayList<Pair> coordinates = new ArrayList<>();
        for (int i = 0; i < ROW_COUNT; i++) {
            for (int j = 0; j < COLUMN_COUNT; j++) {
                coordinates.add(new Pair(i, j));
            }
        }
        Collections.shuffle(coordinates);

        // extract mines (first n coordinates from shuffled list)
        Set<Pair> mines = new HashSet<>();
        for (int i = 0; i < NUM_MINES; i++ ) {
            mines.add(coordinates.get(i));
        }

        return mines;
    }
}