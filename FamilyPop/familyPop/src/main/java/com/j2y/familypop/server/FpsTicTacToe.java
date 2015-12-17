package com.j2y.familypop.server;

import com.j2y.network.client.FpNetFacade_client;
import com.j2y.network.server.FpNetFacade_server;

import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PImage;
import shiffman.box2d.Box2DProcessing;

/**
 * Created by gmpguru on 2015-07-01.
 */
public class FpsTicTacToe
{
    public enum eTictactoeImageIndex
    {   EMPTY(0), O(1), X(2);

        private int value;
        eTictactoeImageIndex(int i)
        {
            value = i;
        }
        public int getValue()
        {
            return value;
        }

        public boolean Compare(int i){return value == i;}
        public static eTictactoeImageIndex getValue(int value)
        {
            eTictactoeImageIndex[] as = eTictactoeImageIndex.values();
            for( int i=0; i<as.length; i++)
            {
                if( as[i].Compare(value))
                {
                    return as[i];
                }
            }

            return eTictactoeImageIndex.EMPTY;
        }
    }
    public boolean _tictactoe_runningMsg_event;

    public int[] _tileIndex;
    private int _boardWidth;
    private int _boardHeight;
    private int _prvIndex;

    private ArrayList< PImage > _ttt_images;
    private PImage _tictactoe_grid;
    //public int _prvStyle;
    public eTictactoeImageIndex _prvStyle = eTictactoeImageIndex.EMPTY;
    public eTictactoeImageIndex _curStyle = eTictactoeImageIndex.EMPTY;

    public void Initialization()
    {
        _tileIndex = new int[]
                { eTictactoeImageIndex.EMPTY.getValue(),eTictactoeImageIndex.EMPTY.getValue(),eTictactoeImageIndex.EMPTY.getValue(),
                        eTictactoeImageIndex.EMPTY.getValue(),eTictactoeImageIndex.EMPTY.getValue(),eTictactoeImageIndex.EMPTY.getValue(),
                        eTictactoeImageIndex.EMPTY.getValue(),eTictactoeImageIndex.EMPTY.getValue(),eTictactoeImageIndex.EMPTY.getValue() };

//        _tileIndex = new int[]
//                { eTictactoeImageIndex.O.getValue(),eTictactoeImageIndex.O.getValue(),eTictactoeImageIndex.O.getValue(),
//                        eTictactoeImageIndex.O.getValue(),eTictactoeImageIndex.O.getValue(),eTictactoeImageIndex.O.getValue(),
//                        eTictactoeImageIndex.O.getValue(),eTictactoeImageIndex.O.getValue(),eTictactoeImageIndex.O.getValue() };


        _prvStyle = eTictactoeImageIndex.EMPTY;
        _prvIndex = -1;
    }

    public void Start(PApplet applet, int boardWidth, int boardHeight)
    {
        _tictactoe_runningMsg_event = false;
        //_ttt_image_empty_red = this.loadImage("image_empty_red.png");
        _ttt_images = new ArrayList<PImage>();

        Initialization();

        _ttt_images.add(applet.loadImage("image_empty_red.png")); // 0  RED_EMPTY
        _ttt_images.add(applet.loadImage("image_ttt_server_o.png")); // 1  RED_O
        _ttt_images.add(applet.loadImage("image_ttt_server_x.png")); // 2  RED_X
        _tictactoe_grid = applet.loadImage("image_ttt_server_grid.png");      // grid

        //_tictactoe_grid.resize(1600, 930);

//        for( int i=0; i<9; i++) {
//            _ttt_images.get(_tileIndex[i]).resize(350, 250);
//        }

        _boardWidth = boardWidth;
        _boardHeight = boardHeight;


        //TicTacToe_tileChange(55, 77, eTictactoeImageIndex.O);

        //TicTacToe_checkSuccessful(eTictactoeImageIndex.O.getValue());
    }
    public void Draw(PApplet applet, Box2DProcessing box2d)
    {
        // grid
        applet.image(_tictactoe_grid, (applet.width - _tictactoe_grid.width) / 2, (applet.height - _tictactoe_grid.height) / 2);

        float centerX = 0.0f;//(this.width - (_ttt_images.get(0).width*3)) / 2;
        float centerY = 0.0f;//(this.height - (_ttt_images.get(0).height*3)) / 2;
        float posX = 0.0f;
        float posY = 0.0f;

        int spacesX = 310; // x 간격
        int spacesY = 110;  // y 간격

        for( int i=0; i<9; i++)
        {
            //centerX = (applet.width - (_ttt_images.get(_tileIndex[i]).width*3)) / 2;
            //centerY = (applet.height - (_ttt_images.get(_tileIndex[i]).height*3)) / 2;

            centerX = (applet.width - ((_ttt_images.get(_tileIndex[i]).width*3)+(spacesX * 2))) / 2;
            centerY = (applet.height - ((_ttt_images.get(_tileIndex[i]).height*3)+(spacesY * 2))) / 2;

            posX = centerX + (_ttt_images.get(_tileIndex[i]).width * (i % 3));
            posY = centerY + (_ttt_images.get(_tileIndex[i]).height * (i/3));

            if( _tileIndex[i] == eTictactoeImageIndex.EMPTY.getValue()) continue;

            applet.image(_ttt_images.get(_tileIndex[i]), posX + ((i % 3) * spacesX), posY + ((i / 3) * spacesY));
        }
    }

    public void TicTacToe_tileChange(int index, eTictactoeImageIndex style )
    {
        if( _tileIndex.length <= index) return;
        if( _tileIndex[index] != eTictactoeImageIndex.EMPTY.getValue() ) return; // 빈슬롯이 아니면 리턴.
        if( _prvStyle == style ) return; // 같은 스타일이면 리턴.

        _tileIndex[index] = style.getValue();

        _prvStyle = style;
        _prvIndex = index;
    }

    public void TicTacToe_tileChange(float x, float y, eTictactoeImageIndex style)
    {
        int index = -1;

        if( x < 0 ) x = 0;
        if( y < 0 ) y = 0;

        if( x > 1) x = 0.9f;
        if( y > 1) y = 0.9f;


        float xTile = _boardWidth / 3;
        float yTile = _boardHeight / 3;

        int xIndex = (int)((x * _boardWidth) / xTile);
        int yIndex = (int)((y * _boardHeight) / yTile);

        index = (yIndex * 3) + xIndex;

        TicTacToe_tileChange(index, style);


//        int tileX = _boardWidth / 3;
//        int tileY = _boardHeight / 3;
//
//        x = x >= _boardWidth ? _boardWidth -tileX : x;
//        y = y >= _boardHeight ? _boardHeight-tileY : y;
//
//        int indX = (x / tileX);
//        int indY = (y / tileY);
//
//        if( indX < 0 || indY < 0 )
//        {
//            indX = indY = 0;
//        }
//
//        index = (indY * 3) + indX;
//
//        TicTacToe_tileChange(index, style);
    }
    public boolean TicTacToe_checkSuccessful(eTictactoeImageIndex style)
    {
        boolean ret  = false;
        if( style == eTictactoeImageIndex.EMPTY) return ret;
        int count;

        for( int j = 0; j<4; j++) // 가로, 세로, 왼쪽위 오른쪽 아래, 오른쪽위 왼쪽 아래.
        {
            for( int y=0; y<3; y++)
            {
                count = 0;

                for( int x=0; x<3; x++)
                {
                    switch (j)
                    {
                        case 0: // 가로
                            if( _tileIndex[(y * 3) + x] == style.getValue() )
                            {
                                count++;
                                if( count >= 3) ret = true;
                            }
                            break;
                        case 1: // 세로
                            if( _tileIndex[(x * 3) + y] == style.getValue() )
                            {
                                count++;
                                if( count >= 3) ret = true;
                            }
                            break;
                        case 2: // 왼쪽 위 에서 오른쪽 아래 (쓸대없이 3번 도네.)
                            if( _tileIndex[(x * 4)] == style.getValue() )
                            {
                                count++;
                                if( count >= 3) ret = true;
                            }
                            break;
                        case 3: // 오른쪽 위 에서 왼쪽 아래 ( 쓸대 없이 3번도네.)
                            if( _tileIndex[(x * 2)+2] == style.getValue() )
                            {
                                count++;
                                if( count >= 3) ret = true;
                            }
                            break;
                    }
                    if( ret) break;
                }
                if( ret) break;
            }
            if( ret) break;
        }

        return ret;
    }
    public void Throwback()
    {
        if( _prvIndex == -1) return;

        _prvStyle =  _tileIndex[_prvIndex] == eTictactoeImageIndex.O.getValue() ?  eTictactoeImageIndex.X : eTictactoeImageIndex.O ;
        _tileIndex[_prvIndex] = eTictactoeImageIndex.EMPTY.getValue();
        _prvIndex = -1;
    }
}
