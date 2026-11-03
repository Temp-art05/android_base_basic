package com.base.app.core.ui.foundation.error

import com.base.app.core.ui.foundation.R
import com.base.app.core.utils.error.DomainException
import org.junit.Assert.assertEquals
import org.junit.Test

class UiErrorTest {

    @Test
    fun `only a real network failure is offline`() {
        assertEquals(ErrorKind.Offline, DomainException.Network().toUiError().kind)
    }

    @Test
    fun `timeouts, server and unknown failures are generic with their own message`() {
        assertEquals(UiError(ErrorKind.Generic, R.string.error_timeout), DomainException.Timeout().toUiError())
        assertEquals(UiError(ErrorKind.Generic, R.string.error_server), DomainException.Server(503).toUiError())
        assertEquals(UiError(ErrorKind.Generic, R.string.error_unexpected), DomainException.Unknown().toUiError())
        assertEquals(UiError(ErrorKind.Generic, R.string.error_unexpected), IllegalStateException().toUiError())
    }
}
