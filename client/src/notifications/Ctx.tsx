// https://gist.github.com/tadeaspetak/f00b60b07183434b30e2ff1edaac56a3

import React, { useState, useCallback, ReactNode, useContext } from "react";
import { nanoid } from "nanoid";

export type NotificationType = "info" | "success" | "error";
export interface Notification {
  id: string;
  message: string;
  type: NotificationType;
}

type NotificationContextProps = {
  notifications: Notification[];
  notify: (message: string, type: NotificationType, id?: string) => void;
  remove: (id: string) => void;
};

export const NotificationContext = React.createContext<
  Partial<NotificationContextProps>
>({});

const rm = (notifications: Notification[], id: string) => {
  const index = notifications.findIndex((n) => n.id === id);
  if (index < 0) return notifications;

  const next = [...notifications];
  next.splice(index, 1);
  return next;
};

export const NotificationProvider = ({ children }: { children: ReactNode }) => {
  const [notifications, setNotifications] = useState<Notification[]>([]);

  const remove = (id: string) => {
    setNotifications(rm(notifications, id));
  };

  const add = (message: string, type: NotificationType, id?: string) => {
    const next = id ? rm(notifications, id) : notifications;
    setNotifications([...next, { id: id || nanoid(6), message, type }]);
  };

  const contextValue: NotificationContextProps = {
    notifications,
    notify: useCallback(add, [notifications]),
    remove: useCallback(remove, [notifications]),
  };

  return (
    <NotificationContext.Provider value={contextValue}>
      {children}
    </NotificationContext.Provider>
  );
};
export function useNotifications() {
  const { notifications, notify, remove } = useContext(NotificationContext);
  if (!notifications || !notify || !remove) {
    throw new Error("Using notificaitons outside of the notification context.");
  }
  return { notifications, notify, remove };
}
