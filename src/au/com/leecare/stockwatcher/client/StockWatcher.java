package au.com.leecare.stockwatcher.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/** Entry point For StockWatcher */
public class StockWatcher implements EntryPoint {
  private VerticalPanel mainPanel = new VerticalPanel();
  private FlexTable stocksFlexTable = new FlexTable();
  private HorizontalPanel addPanel = new HorizontalPanel();
  private TextBox newSymbolTextBox = new TextBox();
  private Button addStockButton = new Button("Add");
  private Label lastUpdatedLabel = new Label();
  private List<String> symbols = new ArrayList<>();

  private static final StockWatcherConstants constants = GWT.create(StockWatcherConstants.class);
  private static final StockWatcherMessages messages = GWT.create(StockWatcherMessages.class);

  private static final int STOCK_SYMBAL_COLUMN = 0;
  private static final int STOCK_PRICE_COLUMN = 1;
  private static final int STOCK_CHANGE_COLUMN = 2;
  private static final int STOCK_REMOVE_COLUMN = 3;
  private static final int REMOVE_BUTTON_COLUMN = 3;
  private static final int REFRESH_INTERVAL = 1_000;

  /** Entry point method. */
  public void onModuleLoad() {
    // Create table for stock data.
    stocksFlexTable.setText(0, STOCK_SYMBAL_COLUMN, constants.symbol());
    stocksFlexTable.setText(0, STOCK_PRICE_COLUMN, constants.price());
    stocksFlexTable.setText(0, STOCK_CHANGE_COLUMN, constants.change());
    stocksFlexTable.setText(0, STOCK_REMOVE_COLUMN, constants.remove());

    stocksFlexTable.getRowFormatter().addStyleName(0, "watchListHeader");
    stocksFlexTable.addStyleName("watchList");

    stocksFlexTable
        .getCellFormatter()
        .addStyleName(0, STOCK_PRICE_COLUMN, "watchListNumericColumn");
    stocksFlexTable
        .getCellFormatter()
        .addStyleName(0, STOCK_CHANGE_COLUMN, "watchListNumericColumn");
    stocksFlexTable
        .getCellFormatter()
        .addStyleName(0, STOCK_REMOVE_COLUMN, "watchListRemoveColumn");

    // Assemble Add Stock panel.
    addPanel.add(newSymbolTextBox);
    addPanel.add(addStockButton);
    addPanel.addStyleName("addPanel");

    // Assemble Main panel.
    mainPanel.add(stocksFlexTable);
    mainPanel.add(addPanel);
    mainPanel.add(lastUpdatedLabel);

    // Associate the Main panel with the HTML host page.
    RootPanel.get("stockList").add(mainPanel);

    newSymbolTextBox.setFocus(true);

    refreshStockTablePeriodically();

    addStockButton.addClickHandler(
        new ClickHandler() {
          @Override
          public void onClick(ClickEvent clickEvent) {
            addStock();
          }
        });

    newSymbolTextBox.addKeyDownHandler(
        new KeyDownHandler() {
          @Override
          public void onKeyDown(KeyDownEvent keyDownEvent) {
            if (keyDownEvent.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
              addStock();
            }
          }
        });
  }

  private void addStock() {
    final String newStock = newSymbolTextBox.getText().toUpperCase().trim();
    newSymbolTextBox.setText("");
    newSymbolTextBox.setFocus(true);

    if (!newStock.matches("^[0-9A-Z\\\\.]{1,10}$")) {
      Window.alert(messages.invalidSymbol(newStock));
      newSymbolTextBox.selectAll();
    }

    if (symbols.contains(newStock)) return;

    int row = stocksFlexTable.getRowCount();
    symbols.add(newStock);
    stocksFlexTable.setText(row, STOCK_SYMBAL_COLUMN, newStock);
    stocksFlexTable.setWidget(row, 2, new Label());
    stocksFlexTable
        .getCellFormatter()
        .addStyleName(row, STOCK_PRICE_COLUMN, "watchListNumericColumn");
    stocksFlexTable
        .getCellFormatter()
        .addStyleName(row, STOCK_PRICE_COLUMN, "watchListNumericColumn");
    stocksFlexTable
        .getCellFormatter()
        .addStyleName(row, STOCK_REMOVE_COLUMN, "watchListRemoveColumn");

    Button removeButton = new Button("x");
    removeButton.addStyleDependentName("remove");
    removeButton.addClickHandler(
        new ClickHandler() {
          @Override
          public void onClick(ClickEvent clickEvent) {
            int removedIndex = symbols.indexOf(newStock);
            symbols.remove(removedIndex);
            stocksFlexTable.removeRow(removedIndex + 1);
          }
        });
    stocksFlexTable.setWidget(row, REMOVE_BUTTON_COLUMN, removeButton);
  }

  private void refreshStockTablePeriodically() {

    Timer refreshTimer =
        new Timer() {
          @Override
          public void run() {
            final double MAX_PRICE = 100.0;
            final double MAX_PRICE_CHANGE = 0.02;

            List<StockPrice> prices = new ArrayList<>();
            for (String stock : symbols) {
              double price = Random.nextDouble() * MAX_PRICE;
              double change = price * MAX_PRICE_CHANGE * (Random.nextDouble() * 2.0 - 1.0);

              prices.add(new StockPrice(stock, price, change));
            }

            updateStockPrice(prices);
          }
        };

    refreshTimer.scheduleRepeating(REFRESH_INTERVAL);
  }

  private void updateStockPrice(List<StockPrice> prices) {
    for (StockPrice stockPrice : prices) {
      if (!symbols.contains(stockPrice.getSymbol())) {
        return;
      }

      int row = symbols.indexOf(stockPrice.getSymbol()) + 1;

      // Format the data in the Price and Change fields.
      String priceText = NumberFormat.getFormat("#,##0.00").format(stockPrice.getPrice());
      NumberFormat changeFormat = NumberFormat.getFormat("+#,##0.00;-#,##0.00");
      String changeText = changeFormat.format(stockPrice.getChange());
      String changePercentText = changeFormat.format(stockPrice.getChangePercent());

      // Populate the Price and Change fields with new data.
      stocksFlexTable.setText(row, STOCK_PRICE_COLUMN, priceText);

      Label changeWidget = (Label) stocksFlexTable.getWidget(row, STOCK_CHANGE_COLUMN);
      changeWidget.setText(changeText + " (" + changePercentText + "%)");

      // Change the color of text in the Change field based on its value.
      String changeStyleName = "noChange";
      if (stockPrice.getChangePercent() < -0.1f) {
        changeStyleName = "negativeChange";
      } else if (stockPrice.getChangePercent() > 0.1f) {
        changeStyleName = "positiveChange";
      }

      changeWidget.setStyleName(changeStyleName);
    }

    lastUpdatedLabel.setText(messages.lastUpdate(new Date()));
  }
}
