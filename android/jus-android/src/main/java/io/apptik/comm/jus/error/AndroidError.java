package io.apptik.comm.jus.error;

import android.content.Intent;

public interface AndroidError {

    Intent getResolutionIntent();

    String getMessage();
}
