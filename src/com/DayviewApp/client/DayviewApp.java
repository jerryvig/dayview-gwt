package com.DayviewApp.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.http.client.*;
import com.google.gwt.jsonp.client.JsonpRequestBuilder;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.DOM;
import com.google.gwt.dom.client.Document;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Timer;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;


import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Entry point classes define <code>onModuleLoad()</code>
 */
public class DayviewApp implements EntryPoint {
    /**
     * This is the entry point method.
     */
    private static final double TOP_BORDER_PCT = 0.08;
    private static final double LEFT_BORDER_PCT = 0.13;
    private static final double TIME_DATE_FONT_PCT = 0.58;
    private static final double HIGHLIGHT_WIDTH = 15;
    private static final double SCROLL_BLOCK_CELLSPACING = 8.0;

    private static DateTimeFormat dateFmt;
    private static DateTimeFormat timeFmt;

    private static int windowHeight;
    private static int windowWidth;
    private static double scrollBlockDivWidth;
    private static double tempFontSize;
    private static double timeDateFontSize;

    private Label timeLabel = null;
    private Label dateLabel = null;
    private FlowPanel scrollBlockPanel = null;
    private FlowPanel weatherPanel = null;
    private FlowPanel trafficPanel = null;
    private Timer timeAndDateTimer = null;
    private ArrayList<Image> imgList = null;

    private FlowPanel temperaturePanel = null;
    private FlexTable weatherInfoTable = null;

    private Logger lager = Logger.getLogger("DayviewLogger");

    public void onModuleLoad() {
        // Assume that the host HTML has elements defined whose
        // IDs are "slot1", "slot2".  In a real app, you probably would not want
        // to hard-code IDs.  Instead, you could, for example, search for all
        // elements with a particular CSS class and replace them with widgets.
        //
        dateFmt = DateTimeFormat.getFormat( "EEE, MMM d" );
        timeFmt = DateTimeFormat.getFormat( "h:mm a" );

        windowHeight = Window.getClientHeight();
        windowWidth = Window.getClientWidth();
        double topBorderPx = Math.floor(windowHeight*TOP_BORDER_PCT);
        double leftBorderPx = Math.floor(windowWidth*LEFT_BORDER_PCT);
        timeDateFontSize = Math.floor(topBorderPx*TIME_DATE_FONT_PCT);
        tempFontSize = timeDateFontSize;
        scrollBlockDivWidth = Math.floor(windowWidth*(1-2.0*LEFT_BORDER_PCT));

        FlowPanel contentPanel = new FlowPanel();
        contentPanel.getElement().getStyle().setTop(topBorderPx, Style.Unit.PX);
        contentPanel.getElement().getStyle().setLeft(leftBorderPx, Style.Unit.PX);
        contentPanel.getElement().getStyle().setPosition(Style.Position.ABSOLUTE);
        timeLabel = new Label( "Time Goes Here" );
        dateLabel = new Label( "Date goes here" );
        timeLabel.getElement().getStyle().setFontSize( timeDateFontSize, Style.Unit.PX );
        dateLabel.getElement().getStyle().setFontSize( timeDateFontSize, Style.Unit.PX );
        timeLabel.setStyleName("timeAndDateLabel");
        dateLabel.setStyleName("timeAndDateLabel");
        contentPanel.add( dateLabel );
        contentPanel.add( timeLabel );

        scrollBlockPanel = new FlowPanel();
        scrollBlockPanel.getElement().getStyle().setPosition(Style.Position.ABSOLUTE);
        scrollBlockPanel.getElement().getStyle().setLeft(leftBorderPx, Style.Unit.PX);
        scrollBlockPanel.getElement().getStyle().setTop(topBorderPx+2.0*timeDateFontSize+15.0, Style.Unit.PX);
        scrollBlockPanel.getElement().getStyle().setWidth(scrollBlockDivWidth, Style.Unit.PX);

        weatherPanel = new FlowPanel();
        weatherPanel.getElement().getStyle().setPosition(Style.Position.ABSOLUTE);
        weatherPanel.getElement().getStyle().setLeft(0, Style.Unit.PX);
        weatherPanel.getElement().getStyle().setTop(HIGHLIGHT_WIDTH, Style.Unit.PX);

        trafficPanel = new FlowPanel();
        trafficPanel.getElement().getStyle().setPosition(Style.Position.ABSOLUTE);
        trafficPanel.getElement().getStyle().setTop(HIGHLIGHT_WIDTH, Style.Unit.PX);

        trafficPanel.add( new Label("here in the traffic panel") );   // here you are.

        scrollBlockPanel.add( weatherPanel );
        scrollBlockPanel.add( trafficPanel );

        RootPanel.get().getElement().getStyle().setTop(0, Style.Unit.PX);
        RootPanel.get().getElement().getStyle().setLeft(0, Style.Unit.PX);
        RootPanel.get().add(contentPanel);
        RootPanel.get().add(scrollBlockPanel);

        printTimeAndDate();
        timeAndDateTimer = new Timer() {
            @Override
            public void run() {
                printTimeAndDate();
            }
        };
        timeAndDateTimer.schedule(2500);

        loadImages();

    }

    public void imagesLoaded() {
        lager.log(Level.SEVERE, "in imagesLoaded()");
        Image weatherBarImg = null;
        Image weatherIconImg = null;

        for ( Iterator<Widget> iter=RootPanel.get().iterator(); iter.hasNext(); ) {
            Widget nextWidget = iter.next();
            if ( nextWidget.getElement().getId().length() > 0 ) {
                lager.log(Level.SEVERE, nextWidget.getElement().getId());
                if ( nextWidget.getElement().getId().equals("weatherBarImg") ) {
                    weatherBarImg = (Image) nextWidget;

                }
                else if ( nextWidget.getElement().getId().equals("weatherIconImg") ) {
                    weatherIconImg = (Image) nextWidget;
                }
            }
        }

        weatherPanel.add( weatherBarImg );
        weatherPanel.add( weatherIconImg );

        double weatherBarImgWidth = Math.floor(scrollBlockDivWidth*0.25);
        double weatherBarImgHeight = Math.floor((weatherBarImgWidth/weatherBarImg.getWidth())*weatherBarImg.getHeight());
        trafficPanel.getElement().getStyle().setLeft(weatherBarImgWidth+SCROLL_BLOCK_CELLSPACING,Style.Unit.PX);

        weatherBarImg.setHeight(Double.toString(weatherBarImgHeight)+"px");
        weatherBarImg.setWidth(Double.toString(weatherBarImgWidth) + "px");
        weatherBarImg.setVisible(true);
        double weatherIconImgHeight = (weatherBarImgWidth/weatherIconImg.getWidth())*weatherIconImg.getHeight();
        weatherIconImg.setWidth(Double.toString(weatherBarImgWidth)+"px");
        weatherIconImg.setHeight(Double.toString(weatherIconImgHeight) + "px");
        weatherIconImg.getElement().getStyle().setPosition(Style.Position.ABSOLUTE);
        weatherIconImg.getElement().getStyle().setLeft(0, Style.Unit.PX);
        weatherIconImg.getElement().getStyle().setTop(0.1*weatherBarImgHeight,Style.Unit.PX);
        weatherIconImg.setVisible(true);

        FlowPanel weatherInfoPanel = new FlowPanel();
        weatherInfoPanel.getElement().getStyle().setPosition(Style.Position.ABSOLUTE);
        weatherInfoPanel.getElement().getStyle().setLeft(10, Style.Unit.PX);
        weatherInfoPanel.getElement().getStyle().setTop(0.1 * weatherBarImgHeight + weatherIconImgHeight, Style.Unit.PX);
        weatherInfoPanel.add( new HTML("Currently:<br/>") );
        temperaturePanel = new FlowPanel();
        temperaturePanel.getElement().getStyle().setFontSize(tempFontSize, Style.Unit.PX);

        weatherInfoTable = new FlexTable();
        weatherInfoTable.setCellPadding(3);
        weatherInfoTable.setCellSpacing(5);

        printWeatherDetails();

        weatherInfoPanel.setStyleName("weatherInfoPanel");
        weatherInfoPanel.add( temperaturePanel );
        weatherInfoPanel.add( weatherInfoTable );


        weatherPanel.add( weatherInfoPanel );

        lager.log(Level.SEVERE, "you arrived here");
    }

    public void printTimeAndDate() {
        Date now = new Date();

        String dateString =  dateFmt.format( now );
        String timeString = timeFmt.format( now );
        timeLabel.setText( timeString );
        dateLabel.setText( dateString );
    }

    public void loadImages() {
        lager.log(Level.SEVERE, "in loadImages()" );
        imgList = new ArrayList<Image>();


        JsonpRequestBuilder jsonpBuilder = new JsonpRequestBuilder();
        jsonpBuilder.requestObject("http://jerry-vm:8080/images", new AsyncCallback<ImagesData>() {
            public void onFailure(Throwable caught) {
                lager.log(Level.SEVERE, "Failure");
            }

            public void onSuccess(ImagesData result) {
                lager.log(Level.SEVERE, "JSONP Success");

                ArrayList<String> imgUrlList = new ArrayList<String>();
                ArrayList<String> imgIdList = new ArrayList<String>();

                if ( result.getImages() != null ) {
                    for ( int i=0; i<result.getImages().length(); i++) {
                        ImageData imgData = result.getImages().get(i);
                        lager.log(Level.SEVERE, imgData.getSrc());
                        imgUrlList.add( imgData.getSrc() );
                        imgIdList.add( imgData.getId() );
                    }

                    lager.log(Level.SEVERE, Integer.toString(imgUrlList.size()));

                    loadImage(0, imgUrlList, imgIdList);
                }
            }
        });
    }

    public void printWeatherDetails() {
        lager.log(Level.SEVERE, "in printWeatherDetails()");
        JsonpRequestBuilder jsonpBuilder = new JsonpRequestBuilder();
        jsonpBuilder.requestObject("http://jerry-vm:8080/weather_details", new AsyncCallback<WeatherData>() {
            public void onFailure(Throwable caught) {
                lager.log(Level.SEVERE, "Failure");
            }

            public void onSuccess(WeatherData result) {
                if (result.getWeatherDetails() != null) {
                    lager.log(Level.SEVERE, "weatherDetails not null");
                    WeatherDetails wd = result.getWeatherDetails().get(0);

                    temperaturePanel.add(new HTML(wd.getCurrentTemperature() + "&ordm;"));
                    weatherInfoTable.setHTML(0, 0, "H: " + wd.getHigh() + "&ordm;");
                    weatherInfoTable.setHTML(0, 1, "L: " + wd.getLow() + "&ordm;");
                    weatherInfoTable.setText(1, 0, "Precip: ");
                    weatherInfoTable.setText(1, 1, wd.getPreciptationChance());
                    weatherInfoTable.setText(2, 0, "Wind: ");
                    weatherInfoTable.setText(2, 1, wd.getWind());
                    weatherInfoTable.setText(3, 0, "Humidity: ");
                    weatherInfoTable.setText(3, 1, wd.getHumidity());
                    weatherInfoTable.setText(4, 0, "UV Index: ");
                    weatherInfoTable.setText(4, 1, wd.getUvIndex());
                }
            }
        });
    }

    public void loadImage( final int index, final ArrayList<String> imgUrlList, final ArrayList<String> imgIdList ) {
        lager.log(Level.SEVERE, "In loadImage() "+index);
        if (index < imgUrlList.size()) {
            Image nextImg = new Image();
            nextImg.setVisible(false);
            nextImg.addLoadHandler(new LoadHandler() {
                @Override
                public void onLoad(LoadEvent evt) {
                    lager.log(Level.SEVERE, "In onLoad()");
                    loadImage(index+1, imgUrlList, imgIdList);

                }
            });
            nextImg.addErrorHandler(new ErrorHandler() {
                @Override
                public void onError(ErrorEvent event) {
                    lager.log(Level.SEVERE, "Failed to load image");
                }
            });
            RootPanel.get().add( nextImg );
            nextImg.getElement().setId(imgIdList.get(index));
            lager.log(Level.SEVERE, "added image to rootpanel");

            nextImg.setUrl(imgUrlList.get(index));
        } else {
            //proceed to next step of page construction.
            imagesLoaded();
        }
    }

    private static class MyAsyncCallback implements AsyncCallback<String> {
        private Label label;

        public MyAsyncCallback(Label label) {
            this.label = label;
        }

        public void onSuccess(String result) {
            label.getElement().setInnerHTML(result);
        }

        public void onFailure(Throwable throwable) {
            label.setText("Failed to receive answer from server!");
        }
    }

    private static class ImageData extends JavaScriptObject {
       protected ImageData() {}

       public final native String getId()  /*-{
          return this.id;
       }-*/;

        public final native String getSrc()  /*-{
            return this.src;
        }-*/;
    }

    private static class ImagesData extends JavaScriptObject {
        protected ImagesData() {}

        public final native JsArray<ImageData> getImages() /*-{
              return this.images;
        }-*/;
    }

    private static class WeatherData extends JavaScriptObject {
        protected WeatherData() {}

        public final native JsArray<WeatherDetails> getWeatherDetails() /*-{
             return this.weatherDetails;
        }-*/;
    }

    private static class WeatherDetails extends JavaScriptObject {
        protected WeatherDetails() {}

        public final native String getCurrentTemperature() /*-{
             return this.currentTemperature;
        }-*/;

        public final native String getHigh() /*-{
            return this.high;
        }-*/;

        public final native String getLow() /*-{
            return this.low;
        }-*/;

        public final native String getPreciptationChance() /*-{
            return this.precipitationChance;
        }-*/;

        public final native String getWind() /*-{
            return this.wind;
        }-*/;

        public final native String getHumidity() /*-{
            return this.humidity;
        }-*/;

        public final native String getUvIndex() /*-{
            return this.uvIndex;
        }-*/;
    }

}
