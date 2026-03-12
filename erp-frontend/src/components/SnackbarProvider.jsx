import { createContext, useContext, useState, useCallback } from "react";
import { Snackbar, Alert } from "@mui/material";

const SnackbarContext = createContext();

export function useSnackbar() {
  return useContext(SnackbarContext);
}

export default function SnackbarProvider({ children }) {
  const [open, setOpen] = useState(false);
  const [msg, setMsg] = useState("");
  const [severity, setSeverity] = useState("success");

  const showSuccess = useCallback((m) => { setMsg(m); setSeverity("success"); setOpen(true); }, []);
  const showError = useCallback((m) => { setMsg(m); setSeverity("error"); setOpen(true); }, []);
  const showWarning = useCallback((m) => { setMsg(m); setSeverity("warning"); setOpen(true); }, []);

  return (
    <SnackbarContext.Provider value={{ showSuccess, showError, showWarning }}>
      {children}
      <Snackbar open={open} autoHideDuration={4000} onClose={() => setOpen(false)} anchorOrigin={{ vertical: "bottom", horizontal: "right" }}>
        <Alert onClose={() => setOpen(false)} severity={severity} variant="filled" sx={{ width: "100%" }}>{msg}</Alert>
      </Snackbar>
    </SnackbarContext.Provider>
  );
}
