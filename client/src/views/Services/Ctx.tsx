import React, { ReactNode, useCallback, useContext, useState } from "react";

export interface IService {
  id: string;
  status: ("ok" | "fail" | "invalid") | null;
  url: string;
}

type Services = Record<string, IService> | undefined;
type ServiceContextProps = {
  services: Services;
  add: (service: IService) => void;
  remove: (id: string) => void;
  set: (services: IService[]) => void;
  updateUrl: (id: string, url: string) => void;
};

export const ServiceContext = React.createContext<Partial<ServiceContextProps>>(
  {},
);

export const ServiceProvider = ({ children }: { children: ReactNode }) => {
  const [services, setServices] = useState<Services>();

  const add = (service: IService) => {
    setServices({ ...services, [service.id]: service });
  };
  const remove = (id: string) => {
    if (!services) return;

    const newServices = { ...services };
    delete newServices[id];
    setServices(newServices);
  };

  const set = (services: IService[]) => {
    const map = services.reduce((acc, service) => {
      acc[service.id] = service;
      return acc;
    }, {} as Record<string, IService>);
    setServices(map);
  };

  const updateUrl = (id: string, url: string) => {
    if (!services) return;

    const newService = services[id];
    if (newService) {
      newService.url = url;
      newService.status = null;
      setServices({ ...services, [id]: newService });
    }
  };

  const contextValue: ServiceContextProps = {
    services,
    add: useCallback(add, [services]),
    remove: useCallback(remove, [services]),
    set: useCallback(set, [services]),
    updateUrl: useCallback(updateUrl, [services]),
  };

  return (
    <ServiceContext.Provider value={contextValue}>
      {children}
    </ServiceContext.Provider>
  );
};
export function useServices() {
  const { services, add, remove, set, updateUrl } = useContext(ServiceContext);
  if (!add || !remove || !set || !updateUrl) {
    throw new Error("Using services outside of the notification context.");
  }
  return { services, add, remove, set, updateUrl };
}
