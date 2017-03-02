package ovh.snacking.snacking.controller.adapter;

import android.app.Activity;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.pdf.PrintedPdfDocument;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Alex on 30/11/2016.
 */

public class InvoicePrintDocumentAdapter extends PrintDocumentAdapter {

    private Activity activity;
    private View mContent;
    private PrintedPdfDocument mPdfDocument;

    public InvoicePrintDocumentAdapter(Activity activity, View view) {
        this.activity = activity;
        this.mContent = view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes, CancellationSignal cancellationSignal, LayoutResultCallback callback, Bundle extras) {
        // New doc
        mPdfDocument = new PrintedPdfDocument(activity, newAttributes);

        // Respond to cancellation request
        if (cancellationSignal.isCanceled() ) {
            callback.onLayoutCancelled();
            return;
        }

        // Return print information to print framework
        PrintDocumentInfo info = new PrintDocumentInfo
                .Builder("print_invoice.pdf")
                .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                .setPageCount(1)
                .build();
        // Content layout reflow is complete
        callback.onLayoutFinished(info, true);
    }

    @Override
    public void onWrite(PageRange[] pages, ParcelFileDescriptor destination, CancellationSignal cancellationSignal, WriteResultCallback callback) {

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(mContent.getWidth(), mContent.getHeight(), 1).create();
        PdfDocument.Page page = mPdfDocument.startPage(pageInfo);

        // Draw view on the page
        mContent.draw(page.getCanvas());

        // Rendering is complete, so page can be finalized.
        mPdfDocument.finishPage(page);

        // check for cancellation
        /*if (cancellationSignal.isCanceled()) {
            callback.onWriteCancelled();
            try {
                mPdfDocument.close();
            } finally {
                mPdfDocument = null;
            }
            return;
        }*/

        try {
            mPdfDocument.writeTo(new FileOutputStream(destination.getFileDescriptor()));
        } catch (IOException e) {
            callback.onWriteFailed((e.toString()));
            return;
        } finally {
            mPdfDocument.close();
            mPdfDocument = null;
        }

        // Signal the print framework the document is complete
        callback.onWriteFinished(pages);

    }

    @Override
    public void onFinish() {
        super.onFinish();
    }
}
