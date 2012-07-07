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
import com.google.gwt.user.client.Element;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.animation.client.Animation;

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

    private FlowPanel contentPanel = null;
    private FlowPanel scrollBlockPanel = null;
    private FlowPanel weatherPanel = null;
    private FlowPanel trafficPanel = null;
    private FlowPanel newsPanel = null;
    private FlowPanel calendarPanel = null;

    private FlowPanel topHighlightPanel = null;
    private FlowPanel bottomHighlightPanel = null;

    private Timer timeAndDateTimer = null;
    private ArrayList<Image> imgList = null;

    private FlowPanel temperaturePanel = null;
    private FlexTable weatherInfoTable = null;
    private FlowPanel newsTextPanel = null;
    private FlowPanel calendarEventPanel = null;

    private boolean moduleMaximized = false;
    private int moduleFocusIndex = 0;
    private FlowPanel focusPanel = null;
    private ArrayList<FlowPanel> focusPanels = new ArrayList();

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

        Element videoElement = DOM.getElementById("mainVid");
        videoElement.getStyle().setPosition(Style.Position.ABSOLUTE);
        videoElement.getStyle().setTop(0, Style.Unit.PX);
        videoElement.getStyle().setLeft(0,Style.Unit.PX);
        videoElement.getStyle().setWidth(windowWidth,Style.Unit.PX);

        double topBorderPx = Math.floor(windowHeight*TOP_BORDER_PCT);
        double leftBorderPx = Math.floor(windowWidth*LEFT_BORDER_PCT);
        timeDateFontSize = Math.floor(topBorderPx*TIME_DATE_FONT_PCT);
        tempFontSize = timeDateFontSize;
        scrollBlockDivWidth = Math.floor(windowWidth*(1-2.0*LEFT_BORDER_PCT));

        contentPanel = new FlowPanel();
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
        scrollBlockPanel.getElement().getStyle().setOverflow(Style.Overflow.VISIBLE);
        scrollBlockPanel.getElement().getStyle().setPosition(Style.Position.RELATIVE);
        scrollBlockPanel.getElement().getStyle().setLeft(0, Style.Unit.PX);
        scrollBlockPanel.getElement().getStyle().setTop(0, Style.Unit.PX);
        scrollBlockPanel.getElement().getStyle().setWidth(scrollBlockDivWidth, Style.Unit.PX);

        weatherPanel = new FlowPanel();
        weatherPanel.getElement().getStyle().setPosition(Style.Position.ABSOLUTE);
        weatherPanel.getElement().getStyle().setLeft(0, Style.Unit.PX);
        weatherPanel.getElement().getStyle().setTop(HIGHLIGHT_WIDTH, Style.Unit.PX);

        trafficPanel = new FlowPanel();
        trafficPanel.getElement().getStyle().setPosition(Style.Position.ABSOLUTE);
        trafficPanel.getElement().getStyle().setTop(HIGHLIGHT_WIDTH, Style.Unit.PX);

        newsPanel = new FlowPanel();
        newsPanel.getElement().getStyle().setPosition(Style.Position.ABSOLUTE);
        newsPanel.getElement().getStyle().setTop(HIGHLIGHT_WIDTH,Style.Unit.PX);

        calendarPanel = new FlowPanel();
        calendarPanel.getElement().getStyle().setPosition(Style.Position.ABSOLUTE);
        calendarPanel.getElement().getStyle().setTop(HIGHLIGHT_WIDTH,Style.Unit.PX);

        topHighlightPanel = new FlowPanel();
        bottomHighlightPanel = new FlowPanel();
        topHighlightPanel.getElement().getStyle().setPosition(Style.Position.ABSOLUTE);
        bottomHighlightPanel.getElement().getStyle().setPosition(Style.Position.ABSOLUTE);

        scrollBlockPanel.add( weatherPanel );
        scrollBlockPanel.add( trafficPanel );
        scrollBlockPanel.add( newsPanel );
        scrollBlockPanel.add( calendarPanel );
        scrollBlockPanel.add( topHighlightPanel );
        scrollBlockPanel.add( bottomHighlightPanel );

        contentPanel.add( scrollBlockPanel );
        RootPanel.get().add(contentPanel);

        printTimeAndDate();
        timeAndDateTimer = new Timer() {
            @Override
            public void run() {
                printTimeAndDate();
            }
        };
        timeAndDateTimer.schedule(2500);

        focusPanels.add( weatherPanel );
        focusPanels.add( trafficPanel );
        focusPanels.add( newsPanel );
        focusPanels.add( calendarPanel );

        loadImages();
    }

    public void imagesLoaded() {
        lager.log(Level.SEVERE, "in imagesLoaded()");
        Image weatherBarImg = null;
        Image weatherIconImg = null;
        Image trafficMapImg = null;
        Image trafficBarImg = null;
        Image newsPhotoImg = null;
        Image newsBarImg = null;
        Image calendarPanelImg = null;
        Image calendarIconImg = null;

        for ( Iterator<Widget> iter=RootPanel.get().iterator(); iter.hasNext(); ) {
            Widget nextWidget = iter.next();
            if ( nextWidget.getElement().getId().length() > 0 ) {
                lager.log(Level.SEVERE, nextWidget.getElement().getId());
                if ( nextWidget.getElement().getId().equals("weatherBarImg") ) {
                    weatherBarImg = (Image)nextWidget;
                }
                else if ( nextWidget.getElement().getId().equals("weatherIconImg") ) {
                    weatherIconImg = (Image)nextWidget;
                }
                else if ( nextWidget.getElement().getId().equals("trafficBottomBarImg") ) {
                    trafficBarImg = (Image)nextWidget;
                }
                else if ( nextWidget.getElement().getId().equals("trafficMapImg") ) {
                    trafficMapImg = (Image)nextWidget;
                }
                else if ( nextWidget.getElement().getId().equals("newPhotoImg") ) {
                    newsPhotoImg = (Image)nextWidget;
                }
                else if ( nextWidget.getElement().getId().equals("newsBottomBarImg") ) {
                    newsBarImg = (Image)nextWidget;
                }
                else if ( nextWidget.getElement().getId().equals("calendarPanelImg") ) {
                    calendarPanelImg = (Image)nextWidget;
                }
                else if ( nextWidget.getElement().getId().equals("calendarIconImg") ) {
                    calendarIconImg = (Image)nextWidget;
                }
            }
        }

        weatherPanel.add( weatherBarImg );
        weatherPanel.add( weatherIconImg );
        trafficPanel.add( trafficMapImg );
        trafficPanel.add( trafficBarImg );
        newsPanel.add( newsPhotoImg );
        newsPanel.add( newsBarImg );
        calendarPanel.add( calendarPanelImg );
        calendarPanel.add( calendarIconImg );

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

        trafficMapImg.setVisible(true);
        trafficBarImg.setVisible(true);

        double trafficScaleFactor = (weatherBarImgHeight/(trafficMapImg.getHeight()+trafficBarImg.getHeight()));
        trafficMapImg.setHeight(Double.toString(Math.floor(trafficScaleFactor*trafficMapImg.getHeight()))+"px");
        trafficMapImg.setWidth(Double.toString(Math.floor(trafficScaleFactor*trafficMapImg.getWidth()))+"px");
        trafficBarImg.setHeight(Double.toString(Math.floor(trafficScaleFactor*trafficBarImg.getHeight()))+"px");
        trafficBarImg.setWidth(Double.toString(Math.floor(trafficScaleFactor*trafficBarImg.getWidth()))+"px");

        trafficMapImg.getElement().getStyle().setPosition(Style.Position.ABSOLUTE);
        trafficBarImg.getElement().getStyle().setPosition(Style.Position.ABSOLUTE);
        trafficMapImg.getElement().getStyle().setTop(0,Style.Unit.PX);
        trafficMapImg.getElement().getStyle().setLeft(0,Style.Unit.PX);
        trafficBarImg.getElement().getStyle().setLeft(0,Style.Unit.PX);
        trafficBarImg.getElement().getStyle().setTop(Math.floor(trafficMapImg.getHeight() * trafficScaleFactor),Style.Unit.PX);

        lager.log(Level.SEVERE, "starting newspanel routine");

        newsPhotoImg.setVisible(true);
        newsBarImg.setVisible(true);
        double newsScaleFactor = (weatherBarImgHeight/(newsPhotoImg.getHeight()+newsBarImg.getHeight()));
        newsPhotoImg.setHeight(Double.toString(Math.floor(newsScaleFactor*newsPhotoImg.getHeight()))+"px");
        newsPhotoImg.setWidth(Double.toString(Math.floor(newsScaleFactor * newsPhotoImg.getWidth())) + "px");
        newsBarImg.setHeight(Double.toString(Math.floor(newsScaleFactor * newsBarImg.getHeight())) + "px");
        newsBarImg.setWidth(Double.toString(Math.floor(newsScaleFactor * newsBarImg.getWidth())) + "px");
        newsPhotoImg.getElement().getStyle().setPosition(Style.Position.ABSOLUTE);
        newsBarImg.getElement().getStyle().setPosition(Style.Position.ABSOLUTE);
        newsPhotoImg.getElement().getStyle().setLeft(0, Style.Unit.PX);
        newsPhotoImg.getElement().getStyle().setTop(0,Style.Unit.PX);
        newsBarImg.getElement().getStyle().setLeft(0,Style.Unit.PX);
        newsBarImg.getElement().getStyle().setTop(Math.floor(newsPhotoImg.getHeight()*newsScaleFactor),Style.Unit.PX);

        newsPanel.getElement().getStyle().setLeft(Math.floor(weatherBarImgWidth+trafficMapImg.getWidth()+2.0*SCROLL_BLOCK_CELLSPACING),Style.Unit.PX);
        newsTextPanel = new FlowPanel();
        newsTextPanel.getElement().getStyle().setPosition(Style.Position.ABSOLUTE);
        newsTextPanel.getElement().getStyle().setLeft(20, Style.Unit.PX);
        newsTextPanel.setWidth(Double.toString(newsPhotoImg.getWidth()-20)+"px");
        newsTextPanel.getElement().getStyle().setTop(Math.floor(newsPhotoImg.getHeight()+60),Style.Unit.PX);
        newsTextPanel.setStyleName("weatherInfoPanel");

        newsPanel.add( newsTextPanel );

        printNewsItems();

        calendarPanelImg.setVisible(true);
        calendarIconImg.setVisible(true);
        double calendarScaleFactor = (weatherBarImgHeight/calendarPanelImg.getHeight());
        calendarPanelImg.setHeight(Double.toString(Math.floor((calendarScaleFactor * calendarPanelImg.getHeight()))) + "px");
        calendarPanelImg.setWidth(Double.toString(Math.floor((calendarScaleFactor * calendarPanelImg.getWidth()))) + "px");
        calendarPanelImg.getElement().getStyle().setPosition(Style.Position.ABSOLUTE);
        calendarPanelImg.getElement().getStyle().setLeft(0, Style.Unit.PX);
        calendarPanelImg.getElement().getStyle().setTop(0, Style.Unit.PX);
        calendarIconImg.getElement().getStyle().setPosition(Style.Position.ABSOLUTE);
        calendarIconImg.getElement().getStyle().setLeft(0,Style.Unit.PX);
        calendarIconImg.getElement().getStyle().setTop(0,Style.Unit.PX);
        calendarIconImg.setWidth(Double.toString(Math.floor(0.18*calendarPanelImg.getHeight()*calendarScaleFactor))+"px");

        calendarPanel.getElement().getStyle().setLeft(Math.floor(weatherBarImgWidth+trafficMapImg.getWidth()+newsPhotoImg.getWidth()+3.0*SCROLL_BLOCK_CELLSPACING),Style.Unit.PX);
        calendarEventPanel = new FlowPanel();
        calendarEventPanel.getElement().getStyle().setPosition(Style.Position.ABSOLUTE);
        calendarEventPanel.getElement().getStyle().setLeft(20, Style.Unit.PX);
        calendarEventPanel.getElement().getStyle().setTop(115, Style.Unit.PX);
        calendarEventPanel.setWidth(Double.toString(calendarPanelImg.getWidth() - 20) + "px");
        calendarPanel.setStyleName("weatherInfoPanel");

        calendarPanel.add( calendarEventPanel );

        printCalendarEvents();

        topHighlightPanel.setWidth(Double.toString(weatherBarImgWidth)+"px");
        topHighlightPanel.setHeight(Double.toString(HIGHLIGHT_WIDTH) + "px");
        topHighlightPanel.getElement().getStyle().setBackgroundColor("#FFEF00");
        topHighlightPanel.getElement().getStyle().setTop(0, Style.Unit.PX);
        topHighlightPanel.getElement().getStyle().setLeft(0,Style.Unit.PX);
        bottomHighlightPanel.setWidth(Double.toString(weatherBarImgWidth)+"px");
        bottomHighlightPanel.setHeight(Double.toString(HIGHLIGHT_WIDTH)+"px");
        bottomHighlightPanel.getElement().getStyle().setTop(weatherBarImgHeight+HIGHLIGHT_WIDTH,Style.Unit.PX);
        bottomHighlightPanel.getElement().getStyle().setLeft(0,Style.Unit.PX);
        bottomHighlightPanel.getElement().getStyle().setBackgroundColor("#FFEF00");
        topHighlightPanel.setStyleName("topHighlightDiv");
        bottomHighlightPanel.setStyleName("bottomHighlightDiv");

        scrollBlockPanel.getElement().getStyle().setZIndex(1000);

        RootPanel.get().addDomHandler(new DayviewKeyDownHandler(),KeyDownEvent.getType());

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

    public void printNewsItems() {
        lager.log(Level.SEVERE, "In printNewsItems()");

        JsonpRequestBuilder jsonpBuilder = new JsonpRequestBuilder();
        jsonpBuilder.requestObject("http://jerry-vm:8080/news_items", new AsyncCallback<NewsItems>() {
            public void onFailure(Throwable caught) {
                lager.log(Level.SEVERE, "Failure");
            }

            public void onSuccess(NewsItems result) {
                if (result.getNewsItems() != null) {
                    lager.log(Level.SEVERE, "news_items not null");

                    for ( int i=0; i<result.getNewsItems().length(); i++ ) {
                        NewsItem ni = result.getNewsItems().get(i);
                        newsTextPanel.add( new HTML("<li>"+ni.getHeadline()+"</li>"));
                    }
                }
            }
        });
    }

    public void printCalendarEvents() {
        lager.log(Level.SEVERE, "In printCalendarEvents()");

        JsonpRequestBuilder jsonpBuilder = new JsonpRequestBuilder();
        jsonpBuilder.requestObject("http://jerry-vm:8080/calendar_events", new AsyncCallback<CalendarEvents>() {
            public void onFailure(Throwable caught) {
                lager.log(Level.SEVERE, "Failure");
            }

            public void onSuccess(CalendarEvents result) {
                if (result.getCalendarEvents() != null) {
                    for ( int i=0; i<result.getCalendarEvents().length(); i++ ) {
                        CalendarEvent ce = result.getCalendarEvents().get(i);
                        calendarEventPanel.add(new HTML(ce.getTimeSlot() + "<br/>" + ce.getTitle()));
                        calendarEventPanel.add( new HTML("<br/>") );
                    }
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

    private static class NewsItems extends JavaScriptObject {
        protected NewsItems() {}

        public final native JsArray<NewsItem> getNewsItems() /*-{
            return this.newsItems;
        }-*/;
    }

    private static class NewsItem extends JavaScriptObject {
        protected NewsItem() {}

        public final native String getHeadline() /*-{
            return this.headline;
        }-*/;
    }

    private static class CalendarEvents extends JavaScriptObject {
        protected CalendarEvents() {}

        public final native JsArray<CalendarEvent> getCalendarEvents() /*-{
            return this.calendarEvents;
        }-*/;
    }

    private static class CalendarEvent extends JavaScriptObject {
        protected CalendarEvent() {}

        public final native String getTitle() /*-{
            return this.title;
        }-*/;

        public final native String getTimeSlot() /*-{
            return this.time_slot;
        }-*/;
    }

    private class DayviewKeyDownHandler implements KeyDownHandler {
        public DayviewKeyDownHandler() {}

        public void onKeyDown(KeyDownEvent evt) {
            if ( evt.getNativeKeyCode() ==  KeyCodes.KEY_RIGHT ) {
                if ( !moduleMaximized ) {
                    if ( moduleFocusIndex < 3 ) {
                       moduleFocusIndex++;
                       processFocusIndex();
                    }
                }
            }
            else if ( evt.getNativeKeyCode() == KeyCodes.KEY_LEFT ) {
                if ( !moduleMaximized ) {
                  if ( moduleFocusIndex > 0 ) {
                     moduleFocusIndex--;
                     processFocusIndex();
                  }
                }
            }
            else if ( evt.getNativeKeyCode() == KeyCodes.KEY_ENTER ) {
                Window.alert( "you pressed the ENTER key" );
            }
            else if ( evt.getNativeKeyCode() == KeyCodes.KEY_ESCAPE ) {
                Window.alert( "you pressed the ESCAPE key" );
            }
        }

        public void processFocusIndex() {
            lager.log(Level.SEVERE, "in processFocusIndex(), moduleFocusIndex = " + moduleFocusIndex );

            focusPanel = focusPanels.get( moduleFocusIndex );
            int widgetCount = focusPanel.getWidgetCount();
            double maxWidth = 0.0;

            for ( int i=0; i<widgetCount; i++ ) {
               if ( focusPanel.getWidget(i).getElement().getStyle().getWidth() != null ) {
                if ( !focusPanel.getWidget(i).getElement().getStyle().getWidth().equals("") ) {
                   String widgetWidthString = focusPanel.getWidget(i).getElement().getStyle().getWidth();
                   double widgetWidth = Double.parseDouble( widgetWidthString.replace("px","") );
                   if ( widgetWidth > maxWidth ) maxWidth = widgetWidth;
                }
               }
            }

            updateHighlightPanels( maxWidth );
        }

        public void updateHighlightPanels( double widgetWidth ) {
            lager.log(Level.SEVERE, "in updateHighlightPanels()");
            double highlightLeft = Double.parseDouble( focusPanel.getElement().getStyle().getLeft().replace("px","") );

            BarAnimation topAnim = new BarAnimation( topHighlightPanel.getElement(),
                                                     Double.parseDouble(topHighlightPanel.getElement().getStyle().getWidth().replace("px","")),
                                                     widgetWidth,
                                                     Double.parseDouble(topHighlightPanel.getElement().getStyle().getLeft().replace("px","")),
                                                     highlightLeft );
            BarAnimation bottomAnim = new BarAnimation( bottomHighlightPanel.getElement(),
                                                        Double.parseDouble(bottomHighlightPanel.getElement().getStyle().getWidth().replace("px","")),
                                                        widgetWidth,
                                                        Double.parseDouble(bottomHighlightPanel.getElement().getStyle().getLeft().replace("px","")),
                                                        highlightLeft );

            topAnim.run(600);
            bottomAnim.run(600);
        }

        public void updateModuleDivPositions() {

        }
    }

    private class BarAnimation extends Animation {
         private final Element e;
         private double widthStart;
         private double widthStop;
         private double leftStart;
         private double leftStop;

         public BarAnimation( Element _e, double _widthStart, double _widthStop, double _leftStart, double _leftStop ) {
             e = _e;
             widthStart = _widthStart;
             widthStop = _widthStop;
             leftStart = _leftStart;
             leftStop = _leftStop;
         }

         @Override
         protected void onUpdate(double prog) {
             double width = widthStart+(widthStop-widthStart)*prog;
             double left = leftStart+(leftStop-leftStart)*prog;
             e.getStyle().setWidth(width, Style.Unit.PX);
             e.getStyle().setLeft(left,Style.Unit.PX);
         }
    }
}
